package site.lghtsg.api.realestates.dataUploader;

import com.amazonaws.util.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.lghtsg.api.common.model.TransactionData;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.realestates.RealEstateDao;
import site.lghtsg.api.realestates.model.RealEstateRemains;
import site.lghtsg.api.realestates.model.RealEstateTransactionData;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import static site.lghtsg.api.config.BaseResponseStatus.*;

@Service
public class AreaPriceCacheUploader {

    @Autowired
    private AreaPriceCacheUploadDao areaPriceCacheUploadDao;

    @Autowired
    private RealEstateDao realEstateDao;

    // 매일 업데이트 되는 부동산의 거래 기록을 가져와 각 지역별 같은 날 1개의 가격만 존재할 수 있도록 처리한다.
    // 동 단위는 불러와서 처리하고, 구, 시 단위는 해당 테이블에 미리 계산 처리해둔다.
    // 데이터 초기화 용
    public void initTableRow() throws BaseException {
        // 날짜 리스트를 가져온다.
        List<String> days = setDateList("2006-01-01", "2023-02-05", "yyyy-MM-dd");
        try{
            areaPriceCacheUploadDao.initAreaPriceCacheRow(days);
        }catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void cachePastPrice() throws BaseException {

        // =================== 데이터 리스트 파일 읽어오기 ====================
        List<String> areaList = new ArrayList<>();
        try {
            String path = "src/main/java/site/lghtsg/api/common/AreaPriceCacheList.txt";
            String line = "";
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));

            while( (line = bufferedReader.readLine()) != null ){
                areaList.add(line);
            }
        }catch(Exception e){
            // throw new
            throw new BaseException(FILE_READ_ERROR);
        }

        // =================== 에러 리스트 파일 열기 ====================
        OutputStream output;
        try {
            output = new FileOutputStream("src/main/java/site/lghtsg/api/common/FailedToCachePastAvg.txt");
        } catch (Exception e) {
            throw new BaseException(FILE_SAVE_ERROR);
        }

//        int test_lim = 1; // 테스트 용 지역 길이 제한 - 서울시 다음부터 시작

        // =================== 각 지역마다 가격 처리 ====================
        for (String area : areaList) {
            String[] area_div = area.split("_");
            int cnt_minimum = getCountMinimum(area_div.length);

            // =================== 데이터 가져오기 및 정렬 ====================
            List<RealEstateTransactionData> prices = realEstateDao.getRealEstatePricesInArea(area);
            if (prices.size() == 0) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);

            // 시간 순 정렬
            prices.sort(Comparator.comparing(TransactionData::getDatetime));

            RealEstateTransactionData lastData = new RealEstateTransactionData();
            lastData.setDatetime("THIS-IS-NOT");
            prices.add(lastData);


            // ===================== 데이터 평균 처리 =======================
            List<RealEstateTransactionData> result = new ArrayList<>();
            long cnt = 0, priceSum = 0;
            String now = "", next = "";
            for (int j = 0, jlim = prices.size(); j < jlim - 1; j++) {
                now = prices.get(j).getDatetime();
                next = prices.get(j + 1).getDatetime();

                priceSum += prices.get(j).getPrice();
                cnt += 1;

                if (now.compareTo(next) != 0) {
                    RealEstateTransactionData data = new RealEstateTransactionData();
                    data.setDatetime(now);
                    if (cnt <= cnt_minimum) priceSum = 0; // 거래 기록이 너무 적을 경우 평균값과 멀어지므로 0으로 가격을 무효시킨다
                    data.setPrice(priceSum / cnt);
                    result.add(data);
                    cnt = 0;
                    priceSum = 0;
                }
            }


            // ===================== 데이터 업데이트 =======================
            List<RealEstateRemains> remains = new ArrayList<>();
            for (RealEstateTransactionData r : result) {
                try {
                    areaPriceCacheUploadDao.updateAreaCacheTable(r, area);
                } catch (Exception e) {
                    // 에러가 나는 데이터들에 한해서는 파일에 넣어준다.
                    String date = r.getDatetime() + " : " + r.getPrice();
                    byte[] by = date.getBytes();
                    try {
                        output.write(by);
                    } catch (Exception ee) {
                        throw new BaseException(FILE_SAVE_ERROR);
                    }
                }
            }
        }
    }

    static int getCountMinimum(int area_level){
        if(area_level >= 2) return 0;
        return 4;
    }

    private List<String> setDateList(String startDate, String endDate, String format) throws BaseException {

        List<String> dateList = new ArrayList<String>();
        SimpleDateFormat formatter = new SimpleDateFormat(format);

        try {
            Calendar beginDate = Calendar.getInstance();
            Calendar stopDate = Calendar.getInstance();

            beginDate.setTime(formatter.parse(startDate));
            stopDate.setTime(formatter.parse(endDate));


            while (beginDate.compareTo(stopDate) != 1) {
                dateList.add(formatter.format(beginDate.getTime()));

                beginDate.add(Calendar.DATE, 1);
            }

        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
        return dateList;
    }
}
