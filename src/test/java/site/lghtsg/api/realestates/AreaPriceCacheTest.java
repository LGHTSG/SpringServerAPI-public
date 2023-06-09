//package site.lghtsg.api.realestates;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import site.lghtsg.api.config.BaseException;
//import site.lghtsg.api.realestates.dataUploader.AreaPriceCacheUploadDao;
//import site.lghtsg.api.realestates.dataUploader.AreaPriceCacheUploader;
//import site.lghtsg.api.realestates.model.RealEstateTransactionData;
//
//import java.util.*;
//
//import static site.lghtsg.api.config.Constant.PARAM_DEFAULT;
//
//@SpringBootTest
//public class AreaPriceCacheTest {
//    @Autowired
//    private RealEstateProvider realEstateProvider;
//    @Autowired
//    private RealEstateDao realEstateDao;
//    @Autowired
//    private AreaPriceCacheUploader areaPriceCacheUploader;
//
//    @Autowired
//    private AreaPriceCacheUploadDao areaPriceCacheDao;
//    @Test
//    void 부동산_구_리스트(){
//        String keyword = PARAM_DEFAULT;
//        try {
//            List<String> result = new ArrayList<>();
//            List<String> regionNames = realEstateProvider.getRegionNames(keyword);
//            for(String elem : regionNames){
//                String [] tmp = elem.split(" ");
//                StringBuilder input = new StringBuilder();
//                for(int i = 0; i < tmp.length - 1; i++){
//                    if(i + 1 == tmp.length - 1){
//                        input.append(tmp[i]);
//                    }
//                    else input.append(tmp[i] + "_");
//                }
//                int flag = 0;
//                for(int i = 0; i < result.size(); i++){
//                    if(result.get(i).equals(input.toString())){
//                        flag = 1; break;
//                    }
//                }
//                if(flag == 0) result.add(input.toString());
//            }
//            for(int i = 0; i < result.size(); i++){
//                System.out.println(result.get(i));
//            }
//            System.out.println(result.size());
//        }
//        catch(BaseException e){
//            System.out.println(e.getStatus());
//        }
//
//    }
//
//    @Test
//    void 파일에서_리스트_읽어오기(){
//        try {
//            areaPriceCacheUploader.cachePastPrice();
//        }catch(BaseException e){
//            e.printStackTrace();
//        }
//    }
//    @Test
//    void 일자_존재하는지_체크(){
//        RealEstateTransactionData r = new RealEstateTransactionData();
//        r.setPrice(20000);
//        r.setDatetime("2016-03-40");
//        realEstateDao.checkDateExists(r);
//    }
//
//    @Test
//    void 테이블초기화(){
//        try {
//            areaPriceCacheUploader.initTableRow();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//}
