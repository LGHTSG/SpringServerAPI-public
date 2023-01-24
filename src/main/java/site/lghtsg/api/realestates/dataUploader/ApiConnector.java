package site.lghtsg.api.realestates.dataUploader;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import site.lghtsg.api.config.Secret.Secret;
import site.lghtsg.api.realestates.dataUploader.model.RealEstate;
import site.lghtsg.api.realestates.dataUploader.model.RealEstateTransaction;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ApiConnector {

    private final RealEstateUploadDao realEstateUploadDao;
    private String authKey = Secret.REALESTATE_PUBLIC_API_AUTHKEY; // api 호출 시 필요한 인증 키

    public ApiConnector(RealEstateUploadDao realEstateUploadDao) {
        this.realEstateUploadDao = realEstateUploadDao;
    }



    /**
     * URL 리스트 생성
     * @return
     * @throws UnsupportedEncodingException
     */
    private List<String> makeUrls() throws UnsupportedEncodingException {

        List<String> urlLinks = new ArrayList<>();
        List<String> regionCodes = realEstateUploadDao.getSigunguCodes();
        String link = "http://openapi.molit.go.kr/OpenAPI_ToolInstallPackage/service/rest/RTMSOBJSvc/getRTMSDataSvcAptTradeDev";

        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

        for (String regionCode : regionCodes) {
            // url build
            StringBuilder urlBuilder = new StringBuilder()
                    .append(link)
                    .append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=" + authKey)
                    .append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")) // 페이지 번호
                    .append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("100000", "UTF-8")) // 페이지당 row 수
                    .append("&" + URLEncoder.encode("LAWD_CD","UTF-8") + "=" + URLEncoder.encode(regionCode, "UTF-8")) // 법정동 시군구 코드
                    .append("&" + URLEncoder.encode("DEAL_YMD","UTF-8") + "=" + URLEncoder.encode(currentMonth, "UTF-8")); // 조회할 시기(월)

            urlLinks.add(urlBuilder.toString());
        }

        return urlLinks;
    }

    /**
     * api 호출, 데이터 line단위로 받아서 readData에 전달 <br>
     * 매일 오전 3시에 실행
     */
    @Async
    @Scheduled(cron = "0 0 3 * * ?")
    public void getData() {
        try {
            // 실행 시간 출력
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            System.out.println("부동산 - 업데이트 실행 시간 : " + now);

            List<String> responses = new ArrayList<>();

            List<String> urlLinks = makeUrls();

            int requestCnt = 0; // 완료된 요청/응답 개수

            for (String urlLink : urlLinks) {

                URL url = new URL(urlLink);
                // connection 세팅
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-type", "text/xml");

                // 응답 데이터 받아오기
                BufferedReader responseReader;
                if(connection.getResponseCode() >= 200 && connection.getResponseCode() <= 300) {
                    responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    responseReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }

                requestCnt++;

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = responseReader.readLine()) != null) {
                    sb.append(line);
                }

                responseReader.close();
                connection.disconnect();

                responses.add(sb.toString());
            }

            updateLastTransactions();

            String result = readData(responses) + " / 보낸 요청 수 : " + requestCnt + "회";
            System.out.println("[" + Thread.currentThread().getName() + "] : " + result);

        } catch (UnsupportedEncodingException encodingException) {
            encodingException.printStackTrace();
            System.out.println("[" + Thread.currentThread().getName() + "] : url 인코딩 실패");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[" + Thread.currentThread().getName() + "] : 데이터 가져오기 실패");
        }
    }

    /**
     * 각 속성값 문자열로 추출
     * @param responses
     * @return
     */
    private String readData(List<String> responses) {
        try {
            // 결과값 List
            List<String[]> rowDatas = new ArrayList<>();

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            for (String response : responses) {

                // String -> document 변환
                Document xmlDoc = builder.parse(new ByteArrayInputStream(response.getBytes()));
                xmlDoc.getDocumentElement().normalize();

                Element root = xmlDoc.getDocumentElement();
                NodeList items = root.getElementsByTagName("item"); // 반환된 item(실거래가) 리스트

                // 데이터 읽어오기
                int length = items.getLength();

                String[] targetTagNames = {"거래금액", "년", "법정동시군구코드", "법정동읍면동코드", "아파트", "월", "일", "전용면적"};

                Itemloop : for (int i = 0; i < length; i++) {
                    Element item = (Element) items.item(i);
                    String[] rowData = new String[8];

                    Attrloop : for (int j = 0; j < targetTagNames.length; j++) {
                        Node valueNode = item.getElementsByTagName(targetTagNames[j]).item(0);
                        String value = (valueNode == null) ? null : valueNode.getTextContent();

                        // null값 있는(잘못된) 데이터는 pass
                        if (value == null) continue Itemloop;

                        rowData[j] = value.trim();
                    }

                    // 전일 자 데이터만 업로드
                    String currentMonth = String.valueOf(LocalDate.now().getMonthValue());
                    String yesterday = String.valueOf(LocalDate.now().getDayOfMonth() - 1);

                    if (rowData[6].equals(yesterday) && rowData[5].equals(currentMonth)) rowDatas.add(rowData);
                }
            }
            String result;

            if (rowDatas.size() > 0) result = createObject(rowDatas) + " row 업데이트";
            else result = "업로드할 데이터 없음";

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return "데이터 읽기 실패";
        }
    }

    private String createObject(List<String[]> rowDatas) {
        // 저장할 값 list
        Set<RealEstate> realEstates = new HashSet<>();
        Set<RealEstateTransaction> transactions = new HashSet<>();

        // realEstates 먼저 업로드 - realEstateId가 없으므로

        for (String[] rowData : rowDatas) {
            Integer regionIdx = Integer.parseInt(rowData[2] + rowData[3].substring(0, 3));

            String realEstateName = rowData[4];

            realEstates.add(RealEstate.builder()
                    .name(realEstateName)
                    .regionId(regionIdx)
                    .build());
        }
        realEstateUploadDao.uploadRealEstates(realEstates);

        // id 생성된 부동산 리스트 가져오기
        List<RealEstate> realEstatesInDB = realEstateUploadDao.getRealEstates();

        for (String[] rowData : rowDatas) {
            Long price = Long.parseLong(rowData[0].replaceAll(",", "")) * 10000;
            float size = Float.parseFloat(rowData[7]);

            int avgPrice = Math.round(price/size);

            int year = Integer.parseInt(rowData[1]);
            int month = Integer.parseInt(rowData[5]);
            int day = Integer.parseInt(rowData[6]);

            LocalDate transactionDate = LocalDate.of(year, month, day);

            // realEstateIdx 찾기
            int realEstateId = -1;

            int regionId = Integer.parseInt(rowData[2] + rowData[3].substring(0, 3));

            for (RealEstate realEstate : realEstatesInDB) {
                if (realEstate.getName().equals(rowData[4]) && realEstate.getRegionId() == regionId) {
                    realEstateId = realEstate.getId();
                    break;
                }
            }

            transactions.add(RealEstateTransaction.builder()
                    .price(avgPrice)
                    .date(transactionDate)
                    .realEstateId(realEstateId)
                    .build());
        }
        realEstateUploadDao.uploadTransactions(transactions);

        return String.valueOf(rowDatas.size());
    }

    public void updateLastTransactions() {
        Set<Integer> realEstateIdxs = realEstateUploadDao.getUpdatedRealEstateIdxs();

        for (Integer realEstateIdx : realEstateIdxs) {
            realEstateUploadDao.updateLastTransactions(realEstateIdx);
        }
    }
}