package site.lghtsg.api.realestates.dataUploader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.lghtsg.api.common.model.TransactionData;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.realestates.RealEstateDao;
import site.lghtsg.api.realestates.model.RealEstateRemains;
import site.lghtsg.api.realestates.model.RealEstateTransactionData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static site.lghtsg.api.config.BaseResponseStatus.FILE_READ_ERROR;
import static site.lghtsg.api.config.BaseResponseStatus.REQUESTED_DATA_FAIL_TO_EXIST;

@Service
public class AreaPriceCacheUploader {

    @Autowired
    private RealEstateDao realEstateDao;

    // 매일 업데이트 되는 부동산의 거래 기록을 가져와 각 지역별 같은 날 1개의 가격만 존재할 수 있도록 처리한다.
    // 동 단위는 불러와서 처리하고, 구, 시 단위는 해당 테이블에 미리 계산 처리해둔다.
    // 데이터 초기화 용
    public void upload() throws BaseException {
        // 1. 가격 데이터를 불러온다
        List<String> areaList = new ArrayList<>();
        try {
            String path = "src/main/java/site/lghtsg/api/common/AreaPriceCacheList.txt";
            String line = "";
            System.out.println("check");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));

            while( (line = bufferedReader.readLine()) != null ){
                areaList.add(line);
            }
        }catch(Exception e){
            // throw new
            throw new BaseException(FILE_READ_ERROR);
        }

        // 쿼리문을 어떻게 날릴지...
        // 일단 과거 데이터만 올리고 자동업로드는 추가로 하자
        // 1. 각 지역별로 데이터 가지고 오기 / 가지고 온 데이터로 값 초기화

        int test_lim = 10; // 테스트 용 지역 길이 제한 - 서울시 다음부터 시작
        // 테이블 값 업데이트만 남았음
        for (int i = 10, ilim = areaList.size(); i < ilim; i++) {
            String area = areaList.get(i);
            List<RealEstateTransactionData> prices = realEstateDao.getRealEstatePricesInArea(area);
            if(prices.size() == 0) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);

            // 시간 순 정렬
            prices.sort(Comparator.comparing(TransactionData::getDatetime));
            // 시간 순 중복 제거 (평균) - 가격 평균을...
            // 날짜가 바뀌는 시점 누적한 평균값을 리스트에 추가한다.
            // 비교용 마지막 더미 데이터
            RealEstateTransactionData lastData = new RealEstateTransactionData();
            lastData.setDatetime("THIS-IS-NOT");
            prices.add(lastData);
            String[] area_div = area.split("_");
            System.out.println(area_div.length);
            int cnt_minimum = getCountMinimum(area_div.length);

            List<RealEstateTransactionData> result = new ArrayList<>();
            long cnt = 0, priceSum = 0;
            String now = "", next = "";
            for(int j = 0, jlim = prices.size(); j < jlim - 1; j++){
                now = prices.get(j).getDatetime();
                next = prices.get(j + 1).getDatetime();

                priceSum += prices.get(j).getPrice();
                cnt += 1;

                if(now.compareTo(next) != 0) {
                    RealEstateTransactionData data = new RealEstateTransactionData();
                    data.setDatetime(now);
                    if(cnt <= cnt_minimum) priceSum = 0; // 거래 기록이 너무 적을 경우 평균값과 멀어지므로 0으로 가격을 무효시킨다
                    data.setPrice(priceSum / cnt);
                    result.add(data);
                    cnt = 0;
                    priceSum = 0;
                }
            }

            // 서울시에 대해서는 계산 끝. 테이블 초기화도 완료
//            if(i == 0) {
//                realEstateDao.initAreaPriceCacheRow(result, area);
//                continue;
//            }

            // 서울 지역 row 초기화
//            try {
//                realEstateDao.initAreaPriceCacheRow(result, area);
//            }catch(Exception e ){
//                throw new BaseException(DATABASE_ERROR);
//            }

            // 데이터 업데이트 파트 - 각 일자의 가격마다 업데이트 쿼리를 짜서 날려야한다.

            List<RealEstateRemains> remains = new ArrayList<>();
            for(RealEstateTransactionData r : result) {
                try {
                    if(realEstateDao.checkDateExists(r) == 1){
                        realEstateDao.updateAreaCacheTable(r, area);
                    }
                    else realEstateDao.insertAreaCacheTable(r, area);
                }
                    catch(Exception e2){
                        RealEstateRemains rm = new RealEstateRemains();
                        rm.setArea(area);
                        rm.setDatetime(r.getDatetime());
                        rm.setPrice(r.getPrice());
                        remains.add(rm);
                }
            }

            for(RealEstateRemains rm : remains){
                System.out.println(rm.getArea() + " / " + rm.getDatetime() + " : " + rm.getPrice());
            }


            // 아직 null 과 0이 왜 동시에 존재히는지 파악하지 못했다.

        }
    }

    static int getCountMinimum(int area_level){
        if(area_level >= 2) return 0;
        return 4;
    }
}
