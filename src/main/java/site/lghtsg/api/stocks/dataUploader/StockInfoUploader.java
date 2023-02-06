package site.lghtsg.api.stocks.dataUploader;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import site.lghtsg.api.stocks.dataUploader.model.StockInfo;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static org.apache.poi.ss.usermodel.CellType.STRING;

@Service
public class StockInfoUploader {

    private final StockUploadDao stockUploadDao;

    public StockInfoUploader(StockUploadDao stockUploadDao) {
        this.stockUploadDao = stockUploadDao;
    }

    public void uploadData() {
        try {
            File folder = new File("C:\\Users\\2High\\Desktop\\주식 종목코드");
            File[] files = folder.listFiles();

            // 엑셀 파일 읽기
            for (File file : files) {
                // 파일 체크
                if (!(file.isFile() && file.canRead())) {
                    System.out.println(file.getName());
                    continue;
                }

                List<StockInfo> stocks = new ArrayList<>();

                XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
                XSSFSheet sheet = workbook.getSheetAt(0);


                int sRowNum = 1;
                int lastRowNum = sheet.getLastRowNum(); // 0-based!!

                int[] targetCellIdxs = {0, 2, (file.getName().contains("kospi") ? 53 : 48)}; // 단축코드, 종목명, 상장주수(코스피 : 53, 코스닥 : 48)

                for (int rNum = sRowNum; rNum <= lastRowNum; rNum++) {
                    XSSFRow row = sheet.getRow(rNum);

                    String[] values = new String[3];
                    String value = "";
                    int valueIdx = 0;

                    for (int idx : targetCellIdxs) {
                        XSSFCell cell = row.getCell(idx);

                        if (cell.getCellType() == STRING) {
                            value = cell.getStringCellValue();
                        } else if (cell.getCellType() == NUMERIC) {
                            value = cell.getNumericCellValue() + "";
                        }

                        values[valueIdx++] = value.trim();
                    }

                    // 값 변환 및 객체 생성
                    long issuedShares = Long.parseLong(values[2].substring(0, values[2].length()-2)) * 1000;

                    StockInfo stockInfo = StockInfo.builder()
                            .name(values[1])
                            .stockCode(values[0])
                            .issuedShares(issuedShares)
                            .build();

                    stocks.add(stockInfo);
                }
                stockUploadDao.uploadDomesticInfo(stocks);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
