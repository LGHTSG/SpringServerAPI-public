//package site.lghtsg.api.user;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import site.lghtsg.api.config.BaseException;
//import site.lghtsg.api.users.UserController;
//import site.lghtsg.api.users.UserDao;
//import site.lghtsg.api.users.UserProvider;
//import site.lghtsg.api.users.UserService;
//import site.lghtsg.api.users.model.*;
//
//import java.util.List;
////import site.lghtsg.api.users.model.GetMyAssetRes;
//
//
//@SpringBootTest
//public class UserTest {
//
//    @Autowired
//    private UserDao userDao;
//    @Autowired
//    private UserProvider userProvider;
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private UserController userController;
//
////    @Test
////    void Test1(){
////        int userIdx = 1;
////        try {
////            List<GetMyAssetRes> getMyAssetRes = userProvider.myAsset(userIdx);
//////            getMyAssetRes.addAll(getMyAssetRes2);
//////            getMyAssetRes.addAll(getMyAssetRes3);
////            for (int i = 0; i < getMyAssetRes.size(); i++){
////                System.out.println(getMyAssetRes.get(i).getAssetName());
////            }
////        }
////        catch(Exception e){
////            System.out.println(e.getMessage());
////        }
////
////    }
//
//    @Test
//    void 해당_자산_과거_거래이력(){
//        int userIdx = 1;
//        PostMyAssetReq postMyAssetReq = new PostMyAssetReq();
//        postMyAssetReq.setAssetIdx(1);
//        postMyAssetReq.setPrice(2000);
//        postMyAssetReq.setCategory("stock");
//        postMyAssetReq.setTransactionTime("2023-01-30 18:02:30");
//        try {
//            Asset previous = userProvider.getPreviousTransaction(userIdx, postMyAssetReq);
//        }
//        catch(BaseException e){
//            System.out.println(e.getStatus());
//        }
//        userDao.getPreviousTransaction(userIdx, postMyAssetReq);
//    }
//
//    @Test
//    void 사용자_자산_구매(){
//        int userIdx = 1;
//        PostMyAssetReq postMyAssetReq = new PostMyAssetReq();
//        postMyAssetReq.setAssetIdx(1);
//        postMyAssetReq.setPrice(2000);
//        postMyAssetReq.setCategory("stock");
//        postMyAssetReq.setTransactionTime("2023-01-30 18:02:30");
//
//        try{
//            userService.buyMyAsset(userIdx, postMyAssetReq);
//        }
//        catch(BaseException e){
//            System.out.println(e.getStatus());
//        }
//    }
//
//    @Test
//    void 사용자_자산_판매(){
//        int userIdx = 1;
//        PostMyAssetReq postMyAssetReq = new PostMyAssetReq();
//        postMyAssetReq.setAssetIdx(1);
//        postMyAssetReq.setPrice(2000);
//        postMyAssetReq.setCategory("realestate");
//        postMyAssetReq.setTransactionTime("2023-01-30 18:02:31");
//
//        try{
//            userService.sellMyAsset(userIdx, postMyAssetReq);
//        }
//        catch(BaseException e){
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    void 사용자_거래이력(){
//        int userIdx = 1;
//        try{
//            String category = "realestat";
//            int assetIdx = 1;
//            List<GetUserTransactionHistoryRes> getUserTransactionHistoryResList = userProvider.getUserTransactionHistory(category, userIdx, assetIdx);
//            for(GetUserTransactionHistoryRes elem : getUserTransactionHistoryResList){
//                System.out.println(elem.getPrice());
//            }
//        }
//        catch (BaseException e){
//            System.out.println(e.getStatus());
//        }
//    }
//
//    @Test
//    void 사용자_자산조회(){
//        int userIdx = 1;
//        userDao.getRealEstateAsset(userIdx);
//        try{
//            List<GetMyAssetRes> getMyAssetRes = userProvider.myAsset(userIdx);
//            for(int i = 0; i < getMyAssetRes.size(); i++){
//                System.out.println(getMyAssetRes.get(i).getAssetName());
//            }
//        }
//        catch(BaseException e){
//            System.out.println(e.getStatus());
//        }
//    }
//
//    @Test
//    void 사용자_등록(){
//        String name = "user";
//        String email = "user2@user.com";
//        int emailCheck = 1;
//        String password = "password";
//        String profileImg = "url";
//
//        PostUserReq user = new PostUserReq(name, email, emailCheck, password, profileImg);
//        try{
//            userDao.createUser(user);
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//
//
//}
//
//
//
