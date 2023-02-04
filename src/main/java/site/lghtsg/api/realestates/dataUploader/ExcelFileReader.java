package site.lghtsg.api.realestates.dataUploader;


import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.realestates.dataUploader.model.RealEstate;
import site.lghtsg.api.realestates.dataUploader.model.RealEstateTransaction;
import site.lghtsg.api.realestates.dataUploader.model.RegionName;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class ExcelFileReader {

    private String dataFolderName = "C:\\Users\\2High\\Desktop\\아파트 실거래가";
    private final RealEstateUploadDao realEstateUploadDao;



    public BaseResponse<String> readData() {
        try {
            // Files 읽어오기
            File folder = new File(dataFolderName);
            File[] files = folder.listFiles();

            List<RegionName> regionNames = realEstateUploadDao.getRegionsForExcel();

            int fileCnt = 0;

            for (File file : files) {
                if (!(file.isFile() && file.canRead())) {
                    System.out.println(file.getName());
                    continue;
                }
                // 읽은 데이터 저장
                List<String[]> rowDatas = new ArrayList<>();

                XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
                XSSFSheet sheet = workbook.getSheetAt(0);

                int firstRowNum = 17; // 0-based
                int lastRowNum = sheet.getLastRowNum();

                int[] targetCellIdxs = {0, 4, 5, 6, 7, 8}; // 지역명(리 포함됨), 건물명, 면적, 년월, 일, 거래금액(만 원)

                for (int rowIdx = firstRowNum; rowIdx < lastRowNum; rowIdx++) {
                    XSSFRow row = sheet.getRow(rowIdx);

                    String[] rowData = new String[6];

                    int valueIdx = 0; // rowData에 값 삽입 시 사용
                    String value = "";

                    for (int idx : targetCellIdxs) {
                        XSSFCell cell = row.getCell(idx);

                        switch (cell.getCellType()) {
                            case STRING:
                                value = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                value = cell.getNumericCellValue() + "";
                        }

                        rowData[valueIdx++] = value.trim().replaceAll(",", "");
                    }
                    rowDatas.add(rowData);
                }
                createObject(rowDatas, regionNames); // 파일 단위로 업로드
                System.out.println(fileCnt++ + "번째 파일 완료, " + LocalDateTime.now());

                workbook.close();
            }

            return new BaseResponse<>("부동산 데이터 업로드 완료");
        } catch (IOException e) {
            e.printStackTrace();
            return new BaseResponse<>("파일 읽기 실패");
        }
    }

    public void createObject(List<String[]> rowDatas, List<RegionName> regionNames) { // {지역명, 건물명, 면적, 년월, 일, 거래금액(만 원)}
        // 업로드할 부동산
        Set<RealEstate> realEstates = new HashSet<>();

        // 같은 부동산, 일 데이터가 있을 때 하나로 합치기 위한 저장공간(key : realEstateIdx + " " + transactionTime, value : transaction)
        Map<String, List<RealEstateTransaction>> transactions = new HashMap<>();


        // realEstate 먼저 업로드
        for (int i = 0; i < rowDatas.size(); i++) {
            // 가공 - 지역명 -> 지역 ID(=regionCode)
            String[] regionSource = rowDatas.get(i)[0].split(" ");
            String last = regionSource[regionSource.length-1];

            if (last.endsWith("리") || last.endsWith(")")) {
                rowDatas.get(i)[0] = String.join(" ", Arrays.copyOfRange(regionSource, 0, regionSource.length-1));
            }

            int regionId = -1;

            for (RegionName regionName : regionNames) {
                if (regionName.getName().equals(rowDatas.get(i)[0])) {
                    regionId = regionName.getLegalCodeId();
                    rowDatas.get(i)[0] = String.valueOf(regionId); // transaction 객체 생성에 재활용
                }
            }

            if (regionId == -1) System.out.println(rowDatas.get(i)[0]);
            else {
                realEstates.add(RealEstate.builder()
                        .name(rowDatas.get(i)[1])
                        .regionId(regionId)
                        .build());
            }

        }

        realEstateUploadDao.uploadRealEstates(realEstates);

        // id 생성된 부동산 리스트
        List<RealEstate> realEstatesInDB = realEstateUploadDao.getRealEstates();

        // transaction 업로드
        for (String[] rowData : rowDatas) {
            // 가공 - price
            Long price = Long.parseLong(rowData[5]) * 10000;
            float size = Float.parseFloat(rowData[2]);

            int avgPrice = Math.round(price/size);

            // 거래일
            String year = rowData[3].substring(0,4);
            String month = rowData[3].substring(4);
            String day = rowData[4];

            String transactionDate = year + "-" + month + "-" + day;

            // 건물 ID
            int realEstateId = -1;

            for (RealEstate realEstate : realEstatesInDB) {

                // 건물명이 같고, 지역도 같으면
                if (realEstate.getName().equals(rowData[1]) && realEstate.getRegionId() == Integer.parseInt(rowData[0])) {
                    realEstateId = realEstate.getId();
                    break;
                }

            }

            String key = realEstateId + " " + transactionDate;

            transactions.putIfAbsent(key, new ArrayList<>());

            transactions.get(key).add(RealEstateTransaction.builder()
                    .price(avgPrice)
                    .date(transactionDate)
                    .realEstateId(realEstateId)
                    .build());
        }

        // 중복 제거된 리스트(부동산마다, 일 최대 1개씩만)
        List<RealEstateTransaction> transactionList = new ArrayList<>();

        // 중복 데이터 합치기
        for (List<RealEstateTransaction> trList : transactions.values()) {
            if (trList.size() == 1) {
                transactionList.add(trList.get(0));
                continue;
            }

            long sumOfPrice = 0;

            for (RealEstateTransaction tr : trList) {
                sumOfPrice += tr.getPrice();
            }
            int price = (int) (sumOfPrice/trList.size());
            trList.get(0).setPrice(price);

            transactionList.add(trList.get(0));
        }

        // transactions 업로드
        Set<Integer> updatedRealEstateIdxs = realEstateUploadDao.uploadTransactions(transactionList);
        // lastTrs & Transaction 테이블들 업데이트
        realEstateUploadDao.updateTrs(updatedRealEstateIdxs);
    }

//    /**
//     * 최조 업데이트(기존 구조 -> TodayTrans 추가된 구조) 시 사용한 메소드
//     * @return
//     */
//    public String updateLastTrs_NEW() {
//        try {
//            realEstateUploadDao.updateLastTrs_NEW();
//            return "success";
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "failed";
//        }
//    }
}