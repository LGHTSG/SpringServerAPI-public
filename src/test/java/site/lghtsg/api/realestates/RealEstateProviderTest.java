//package site.lghtsg.api.realestates;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import site.lghtsg.api.common.model.Box;
//import site.lghtsg.api.config.BaseException;
//import site.lghtsg.api.realestates.model.RealEstateBox;
//import site.lghtsg.api.realestates.model.RealEstateTransactionData;
//
//import java.util.List;
//
//import static site.lghtsg.api.config.Constant.*;
//
//@SpringBootTest
//public class RealEstateProviderTest {
//
//    @Autowired
//    private RealEstateProvider realEstateProvider;
//    @Autowired
//    private RealEstateDao realEstateDao;
//
//    @Test
//    void 부동산_정렬조회(){
////        String area = "서울특별시";
//        String area = PARAM_DEFAULT;
//        String sort = SORT_FLUCTUATION_PARAM;
//        String order = ASCENDING_PARAM;
//
//        final int test_len = 10;
//        List<RealEstateBox> realEstateBoxList;
//        try {
//            for(int i = 0; i < test_len; i++) {
//                long start = System.currentTimeMillis();
//                realEstateBoxList = realEstateProvider.getRealEstateBoxes(sort, order, area);
//                long end = System.currentTimeMillis();
//
//                // results
//                System.out.println("Test #" + (i + 1));
//                System.out.println("Data Length : " + realEstateBoxList.size() + "개의 데이터");
//                System.out.println("Duration : " + (double) (end - start) / 1000 + "s");
//            }
//        }
//        catch(Exception e){
//            System.out.println(e.getMessage());
//        }
//    }
//
//    @Test
//    void 부동산_지역포함개선_테스트(){
////        String area = "대전광역시";
//        String area = "경기도 안산시 상록구"; // 에전같았으면.. 그냥 수원시부터 안됐음..
//        String sort = SORT_FLUCTUATION_PARAM;
//        String order = ASCENDING_PARAM;
//        realEstateDao.getRealEstateBoxesInArea(area);
//        try{
//            List<RealEstateBox> realEstateBoxes = realEstateProvider.getRealEstateBoxes(sort, order, area);
//            for(RealEstateBox realEstateBox : realEstateBoxes){
//                System.out.println(realEstateBox.getName() + " / " + realEstateBox.getPrice());
//            }
//        }catch(BaseException e){
//            System.out.println(e.getStatus());
//        }
//    }
//
//    @Test
//    void 부동산_캐시_리스트_테스트(){
//        String area = "대전광역시+유성구";
//        try {
//            List<RealEstateTransactionData> data = realEstateProvider.getAreaRealEstatePrices(area);
//            System.out.println(area);
//            System.out.println(data.size());
//            for(RealEstateTransactionData r : data) {
//                System.out.println(r.getDatetime() + " : " + r.getPrice());
//            }
//        }catch(Exception e){
//            e.getStackTrace();
//        }
//    }
//
//    @Test
//    void 특정_부동산_가격조회(){
//        long idx = 1;
//        try{
//            List<RealEstateTransactionData> realEstateTransactionData = realEstateProvider.getRealEstatePrices(idx);
//            for(RealEstateTransactionData r : realEstateTransactionData){
//                System.out.println(r.getDatetime() + " " + r.getPrice());
//            }
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//}
