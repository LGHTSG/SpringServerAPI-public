package site.lghtsg.api.realestates.dataUploader;


import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.realestates.RealEstateDao;
import site.lghtsg.api.realestates.model.upload.RealEstate;
import site.lghtsg.api.realestates.model.upload.RealEstateTransaction;
import site.lghtsg.api.realestates.model.upload.RegionName;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@RequiredArgsConstructor
@Service
public class ExcelFileReader {

    private String dataFolderName = "C:\\Users\\2High\\Desktop\\아파트 실거래가";
    private final RealEstateDao realEstateDao;



    public BaseResponse<String> readData() {
        try {

            // Files 읽어오기
            File folder = new File(dataFolderName);
            File[] files = folder.listFiles();

            List<RegionName> regionNames = realEstateDao.getRegionsForExcel();

            int cnt = 0;

            for (File file : files) {
                if (cnt++ > 1) break;
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
            }
            return new BaseResponse<>("부동산 데이터 업로드 완료");
        } catch (IOException e) {
            e.printStackTrace();
            return new BaseResponse<>("파일 읽기 실패");
        }
    }

    public void createObject(List<String[]> rowDatas, List<RegionName> regionNames) { // {지역명, 건물명, 면적, 년월, 일, 거래금액(만 원)}

        Set<RealEstate> realEstates = new HashSet<>();


        List<RealEstateTransaction> transactions = new ArrayList<>();

        // realEstate 먼저 업로드
        for (String[] rowData : rowDatas) {
            // 가공 - 지역명 -> 지역 ID(=regionCode)
            String[] regionSource = rowData[0].split(" ");
            String last = regionSource[regionSource.length-1];

            if (last.endsWith("리") || last.endsWith(")")) {
                rowData[0] = String.join(" ", Arrays.copyOfRange(regionSource, 0, regionSource.length-1));
            }

            int regionId = -1;

            for (RegionName regionName : regionNames) {
                if (regionName.getName().equals(rowData[0])) {
                    regionId = regionName.getLegalCodeId();
                }
            }





            if (regionId == -1) System.out.println(rowData[0]);
            else {
                realEstates.add(RealEstate.builder()
                        .name(rowData[1])
                        .regionId(regionId)
                        .build());
            }

        }

        realEstateDao.uploadRealEstates(realEstates);

        // id 생성된 부동산 리스트
        List<RealEstate> realEstatesInDB = realEstateDao.getRealEstates();

        // transaction 업로드
        for (String[] rowData : rowDatas) {
            // 가공 - price
            Long price = Long.parseLong(rowData[5]) * 10000;
            float size = Float.parseFloat(rowData[2]);

            int avgPrice = Math.round(price/size);

            // 거래일
            int year = Integer.parseInt(rowData[3].substring(0,4));
            int month = Integer.parseInt(rowData[3].substring(4));
            int day = Integer.parseInt(rowData[4]);

            LocalDate transactionDate = LocalDate.of(year, month, day);

            // 건물 ID
            int realEstateId = -1;

            for (RealEstate realEstate : realEstatesInDB) {

                if (realEstate.getName().equals(rowData[1])) {
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

        realEstateDao.uploadTransactions(transactions);
    }
}
