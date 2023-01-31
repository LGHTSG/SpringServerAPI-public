package site.lghtsg.api.realestates;

import org.checkerframework.common.value.qual.StaticallyExecutable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.lghtsg.api.realestates.model.RealEstateBox;
import site.lghtsg.api.realestates.model.RealEstateTransactionData;

import java.util.Collections;
import java.util.List;

import static site.lghtsg.api.config.Constant.*;

@SpringBootTest
public class RealEstateDaoTest {

    @Autowired
    private RealEstateDao realEstateDao;

    @Test
    void 부동산_리스트_반환(){
//        resellDao.scraping();
    }

    @Test
    void 부동산_거래기록_전체조회_및_정렬(){
        List<RealEstateTransactionData> realEstateTransactionData = realEstateDao.getAllTransactionData();
        Collections.sort(realEstateTransactionData);
        for(int i = 0, lim = realEstateTransactionData.size(); i < lim; i++){
            System.out.println(realEstateTransactionData.get(i));
        }
    }


    @Test
    void 모든_부동산_조회() {
        final int test = 10;

        String sort = SORT_PRICE_PARAM;
        String order = ASCENDING_PARAM;
        String area = PARAM_DEFAULT;

        long startTime = 0, endTime = 0;
        for (int i = 0; i < test; i++) {
            long start = System.currentTimeMillis();
//            try {
            List<RealEstateBox> realEstateBoxes = realEstateDao.getAllRealEstateBoxes();

            long end = System.currentTimeMillis();

            System.out.println("Test #" + (int) (i + 1));
            System.out.println("Data length : " + realEstateBoxes.size());
            System.out.println("Total Duration : " + (double) (end - start) / 1000 + "s");
            System.out.println();
            startTime += start;
            endTime += end;
//            }
//            catch(BaseException e){
//                System.out.println(e.getStatus());
//            }

        }
        startTime /= test;
        endTime /= test;

        System.out.println("==== Test Results ====");
        System.out.println("Avg Duration : " + (double) (endTime - startTime) / 1000 + "s");
        System.out.println();
    }

    @Test
    void 특정_지역내_부동산_조회_및_정럴(){
//        final int test = 10;
//
//        String sort = null;
//        String order = ASCENDING_PARAM;
//        String area = "서울특별시";
//
//        long startTime = 0, afterReadTime = 0, endTime = 0;
//
//        for(int i = 0; i < test; i++){
//            long start = System.currentTimeMillis();
//
//            List<RealEstateBox> realEstateBoxes = realEstateDao.getRealEstateBoxesInArea(area);
//            long afterRead = System.currentTimeMillis();
//
//            Collections.sort(realEstateBoxes);
//            long end = System.currentTimeMillis();
//
//            System.out.println("Test #" + (int)(i + 1));
//            System.out.println("Data length : " + realEstateBoxes.size());
//            System.out.println("Total Duration : " + (double)(end - start) / 1000 + "s");
//            System.out.println("Read Duration : " + (double)(afterRead - start) / 1000 + "s");
//            System.out.println("Sort Duration : " + (double)(end - afterRead) / 1000 + "s");
//            System.out.println();
//            startTime += start; afterReadTime += afterRead; endTime+= end;
//        }
//        startTime /= test; afterReadTime /= test; endTime /= test;
//
//
//        System.out.println("==== Test Results ====");
//        System.out.println("Avg Duration : " + (double)(endTime - startTime) / 1000 + "s");
//        System.out.println("Avg Read Duration : " + (double)(afterReadTime - startTime) / 1000 + "s");
//        System.out.println("Avg Sort Duration : " + (double)(endTime - afterReadTime) / 1000 + "s");
//        System.out.println();

    }

    @Test
    void 데이터베이스_데이터_유실_테스트(){
//        List<RealEstateDao.Data> reIdx = realEstateDao.getAllRealEstateIdxFromRE();
//        List<RealEstateDao.Data> retIdx = realEstateDao.getAllRealEstateIdxFromRET();
//        System.out.println("RealEstateTransaction Size : " + retIdx.size());
//        System.out.println("RealEstate Size : " + reIdx.size());
//        Collections.sort(retIdx);
//        int cntErr = 0, cntCorrent = 0;
//        for(RealEstateDao.Data target : reIdx){
//            int left = 0, right = retIdx.size() - 1;
//            int flag = 0;
//            while(left <= right){
//                int mid = (left + right) / 2;
//                if(target.idx == retIdx.get(mid).idx) {
//                    flag = 1;
//                    cntCorrent += 1;
//                    break;
//                }
//                else if(target.idx > retIdx.get(mid).idx){
//                    left = mid + 1;
//                }
//                else {
//                    right = mid - 1;
//                }
//            }
//            if(flag == 0) cntErr += 1;
//        }
//        System.out.println("Error cnt : " + cntErr);
//        System.out.println("Correct cnt : " + cntCorrent);
    }

//    @Test
//    void uploadData(){
//        Set<RealEstate> realEstates = new HashSet<RealEstate>();
//
//        realEstates.add(RealEstate.builder()
//                .regionId(29170127)
//                .name("test")
//                .build());
//        realEstates.add(RealEstate.builder()
//                .regionId(29170127)
//                .name("test2")
//                .build());
//
//        realEstateDao.uploadRealEstates(realEstates);
//    }

    @Test
    void 부동산_지역내_모든가격_리스트(){
        String area = "서울특별시 강남구";
        final int test_len = 10;
        for(int i = 0; i < test_len; i++){

            long start = System.currentTimeMillis();
            List<RealEstateTransactionData> realEstateTransactionData = realEstateDao.getRealEstatePricesInArea(area);
            long end = System.currentTimeMillis();
            System.out.println("Duration : " + (double)(end - start) / 1000 + "s");

        }

    }
}
//