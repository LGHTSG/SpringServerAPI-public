package site.lghtsg.api.stocks.dataUploader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import site.lghtsg.api.config.Secret.Secret;
import site.lghtsg.api.stocks.dataUploader.model.StockInfo;
import site.lghtsg.api.stocks.dataUploader.model.StockTransaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 자동 업로드 테스트를 위한 클래스
 */
@Service
public class StockApiConnectorCopy {

    private final StockUploadDaoCopy stockUploadDao;
    private final String appKey = Secret.STOCK_KIS_API_APPKEY;
    private final String appSecret = Secret.STOCK_KIS_API_APPSECRET;
    private String accessToken;


    public StockApiConnectorCopy(StockUploadDaoCopy stockUploadDao) {
        this.stockUploadDao = stockUploadDao;
    }


    /**
     * 테스트 Table에 업로드
     */
//    @Async
//    @Scheduled(cron = "0 0 17 ? * MON-FRI") // UTC 기준. 한국시간 기준 TUE-SAT 2AM
    public void getClosePricesOfDomestic() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String sTime = format.format(new Date());

            // 시작 시간 표기
            stockUploadDao.insertTimeFlag(sTime);

            // 실행
            getDataOfDomestic();

            // 종료 시간 표기
            String eTime = format.format(new Date());
            stockUploadDao.insertTimeFlag(eTime);

            System.out.println("sTime = " + sTime);
            System.out.println("eTime = " + eTime);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("국내 일간종목 종가 가져오기 실패");
        }
    }

    // 소스코드

    /**
     * 일 단위 종목 업데이트
     * @throws IOException
     * @throws ParseException
     * @throws InterruptedException
     */
    private void getDataOfDomestic() throws IOException, ParseException, InterruptedException {
        updateAccessToken();

        String[] headerKeys = {"content-type", "authorization", "appkey", "appsecret", "tr_id"};
        String[] headerValues = {"application/json", "Bearer " + this.accessToken, appKey, appSecret, "FHKST03010100"};

        // 쿼리 파라미터 - 시장구분코드, 종목코드, 시작일, 종료일, 기간 타입, 가격 수정(보정) 여부
        String[] paramKeys = {"fid_cond_mrkt_div_code", "fid_input_iscd", "fid_input_date_1", "fid_input_date_2", "fid_period_div_code", "fid_org_adj_prc"};
        String[] paramValues = {"J", "0053930", "yyyyMMdd", "yyyyMMdd", "D", "0"};

        // 조회할 종목
        List<StockInfo> stocks = stockUploadDao.getNotServicedStockInfos();

        // 조회기간
//        List<List<String>> periods = getPeriods(yesterday, LocalDate.of(2018, 1, 1));

//        LocalDate tempDate = LocalDate.of(2023, 2, 11);
        List<List<String>> periods = getPeriods(LocalDate.now(), LocalDate.now());

        List<String> startDates = periods.get(0);
        List<String> endDates = periods.get(1);

        // 업로드할 tr
        List<StockTransaction> transactions = new ArrayList<>();

        for (int i = 0; i < startDates.size(); i++) {
            paramValues[2] = startDates.get(i);
            paramValues[3] = endDates.get(i);

            for (StockInfo stock : stocks) {
                paramValues[1] = stock.getStockCode();

                //  쿼리스트링 세팅
                // url(국내주식)
                String urlLink = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";

                String queryString = makeQueryString(paramKeys, paramValues);
                urlLink += queryString;

                URL url = new URL(urlLink);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // 헤더 세팅
                for (int j = 0; j < headerKeys.length; j++) {
                    conn.setRequestProperty(headerKeys[j], headerValues[j]);
                }

                // 연결
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                Thread.sleep(60);

                conn.connect();

                StringBuilder sb = new StringBuilder();
                String line = "";

                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                conn.disconnect();

                // 데이터 읽기
                JSONParser parser = new JSONParser();

                JSONObject response = (JSONObject) parser.parse(sb.toString());
                JSONArray output2 = (JSONArray) response.get("output2");

                if (output2.size() == 0) continue;

                for (Object o : output2) {
                    JSONObject output = (JSONObject) o;

                    if (output.get("stck_bsop_date") == null && output.get("stck_clpr") == null) continue; // 빈 아이템 ({}) 인 경우

                    int tradingVol = Integer.parseInt(output.get("acml_vol").toString());
                    int price = Integer.parseInt(output.get("stck_clpr").toString());

                    // 시간 형식 맞추기
                    String trTimeSrc = output.get("stck_bsop_date").toString();
                    String transactionTime = trTimeSrc.substring(0, 4) + "-" + trTimeSrc.substring(4, 6) + "-" + trTimeSrc.substring(6)
                            + " 16:00:00";

                    StockTransaction transaction = StockTransaction.builder()
                            .stockIdx(stock.getStockIdx())
                            .transactionTime(transactionTime)
                            .tradingVol(tradingVol)
                            .price(price)
                            .build();

                    transactions.add(transaction);
                }
            }
            System.out.println(paramValues[2] + " ~ " + paramValues[3]);
        }
        // 데이터 업로드
        Set<Integer> updatedStockIdxs = stockUploadDao.uploadPrices(transactions);

        // lastTrs 업데이트
        stockUploadDao.updateTrsOfDaily(updatedStockIdxs);
        System.out.println("updatedStockIdxs.size() = " + updatedStockIdxs.size());
    }

    /**
     * 해외종목 가격 조회(일별)
     * @throws IOException
     * @throws ParseException
     * @throws InterruptedException
     */
    public void getClosePricesOfAmerican() throws IOException, ParseException, InterruptedException {
        updateAccessToken();
        int exchangeRate = 1230;

        // 헤더
        String[] headerKeys = {"content-type", "authorization", "appkey", "appsecret", "tr_id"};
        String[] headerValues = {"application/json", "Bearer " + this.accessToken, appKey, appSecret, "FHKST03030100"};

        // 쿼리 파라미터 - 시장구분코드, 종목코드, 시작일, 종료일, 기간 타입, 가격 수정(보정) 여부
        String[] paramKeys = {"fid_cond_mrkt_div_code", "fid_input_iscd", "fid_input_date_1", "fid_input_date_2", "fid_period_div_code"};
        String[] paramValues = {"N", "HD", "20220101", "20220103", "D"};

        String baseUrl = "https://openapi.koreainvestment.com:9443/uapi/overseas-price/v1/quotations/inquire-daily-chartprice";

//        // 기간 단위 조회
//        List<List<String>> periods = getPeriods(LocalDate.now(), LocalDate.of(2018, 1, 1));

        // 일 단위 조회
        LocalDate yesterday = LocalDate.now().minusDays(1); // 현지시간 2/1 데이터가, 한국시간 2/3일에 조회 가능 -> 1일 추가로 빼줘야.
        List<List<String>> periods = getPeriods(yesterday, yesterday);

        List<String> startDates = periods.get(0);
        List<String> endDates = periods.get(1);

        // 업로드할 tr
        List<StockTransaction> transactions = new ArrayList<>();

        // 조회 종목(S&P 500)
        List<StockInfo> stockInfos = stockUploadDao.getSNPStockInfos();

        for (int i = 0; i < startDates.size(); i++) {
            paramValues[2] = startDates.get(i);
            paramValues[3] = endDates.get(i);


            for (StockInfo stock : stockInfos) {
                paramValues[1] = stock.getStockCode();
                // 쿼리스트링 세팅
                String queryString = makeQueryString(paramKeys, paramValues);

                String urlLink = baseUrl + queryString;
                URL url = new URL(urlLink);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // 헤더 세팅
                for (int j = 0; j < headerKeys.length; j++) {
                    conn.setRequestProperty(headerKeys[j], headerValues[j]);
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                Thread.sleep(70);

                conn.connect();

                StringBuilder sb = new StringBuilder();
                String line = "";

                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                conn.disconnect();

                JSONParser parser = new JSONParser();

                JSONObject response = (JSONObject) parser.parse(sb.toString());
                JSONArray output2 = (JSONArray) response.get("output2");

                if (output2.size() == 0) continue;

                for (Object o : output2) {
                    JSONObject output = (JSONObject) o;

                    if (output.get("stck_bsop_date") == null || output.get("ovrs_nmix_prpr") == null
                            || output.get("acml_vol") == null) continue; // 잘못된 아이템인 경우

                    int tradingVol = Integer.parseInt(output.get("acml_vol").toString());

                    int price = (int) (Float.parseFloat(output.get("ovrs_nmix_prpr").toString()) * exchangeRate);

                    // 시간 한국 기준으로 통일 (+14시간)
                    String trTimeSrc = output.get("stck_bsop_date").toString();

                    LocalDate date = LocalDate.parse(trTimeSrc, DateTimeFormatter.ofPattern("yyyyMMdd"));
                    LocalDate koreanDate = date.plusDays(1);

                    String transactionTime = koreanDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 06:00:00";

                    StockTransaction transaction = StockTransaction.builder()
                            .stockIdx(stock.getStockIdx())
                            .transactionTime(transactionTime)
                            .tradingVol(tradingVol)
                            .price(price)
                            .build();

                    transactions.add(transaction);
                }
            }
            System.out.println(paramValues[2] + " ~ " + paramValues[3]);
        }
        // 데이터 업로드
        Set<Integer> updatedStockIdxs = stockUploadDao.uploadPrices(transactions);

        // lastTrs 업데이트
        stockUploadDao.updateTrsOfDaily(updatedStockIdxs);
        System.out.println("updatedStockIdxs.size() = " + updatedStockIdxs.size());
    }

    /**
     * 기간 (시작일, 종료일 리스트) 반환
     * @param sDate
     * @param destDate
     * @return periods
     */
    private List<List<String>> getPeriods(LocalDate sDate, LocalDate destDate) {

        List<List<String>> periods = new ArrayList<>(); // 반환할 리스트
        List<String> startDates = new ArrayList<>();
        List<String> endDates = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        if (sDate.isEqual(destDate)) { // 하루 조회

            String yesterday = formatter.format(sDate.minusDays(1));
            startDates.add(yesterday);
            endDates.add(yesterday);

        } else { // 기간 조회

            while (sDate.isAfter(destDate)) {
                endDates.add(formatter.format((sDate = sDate.minusDays(1))));
                startDates.add(formatter.format((sDate = sDate.minusDays(95))));
            }

            if (!(sDate.isEqual(destDate))) {
                startDates.remove(startDates.size()-1);
                startDates.add(formatter.format(destDate));
            }
        }
        periods.add(startDates);
        periods.add(endDates);

        return periods;
    }

    public void updateAccessToken() throws IOException, ParseException {

        String urlLink = "https://openapi.koreainvestment.com:9443/oauth2/tokenP";

        String[] paramKeys = {"grant_type", "appkey", "appsecret"};
        String[] paramValues = {"client_credentials", appKey, appSecret};

        // request body 생성
        String body = makeRequestBody(paramKeys, paramValues);

        URL url = new URL(urlLink);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");

        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] request_data = body.getBytes("utf-8");
            os.write(request_data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 연결
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        conn.connect();

        StringBuilder response = new StringBuilder();

        String line = "";

        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();

        JSONObject responseData = (JSONObject) new JSONParser().parse(response.toString());

        this.accessToken = responseData.get("access_token").toString();
        System.out.println(accessToken);

//        return accessToken;
    }


    private String makeRequestBody(String[] keys, String[] values) {
        String bodyStr = "{\n";

        for (int i = 0; i < keys.length; i++) {
            bodyStr += " \"" + keys[i] + "\" : \"" + values[i] + "\",\n";
        }

        return bodyStr.substring(0, bodyStr.length()-2) + "\n}";

    }

    private String makeQueryString(String[] keys, String[] values) throws IOException {
        String result = "?";

        for (int i = 0; i < keys.length; i++) {
            result += URLEncoder.encode(keys[i], "UTF-8") + "=" + URLEncoder.encode(values[i], "UTF-8") + "&";
        }
        return result.substring(0, result.length()-1);
    }
}
