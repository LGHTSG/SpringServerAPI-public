package site.lghtsg.api.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.users.UserController;
import site.lghtsg.api.users.UserDao;
import site.lghtsg.api.users.UserProvider;
import site.lghtsg.api.users.UserService;
import site.lghtsg.api.users.model.PostMyAssetReq;
//import site.lghtsg.api.users.model.GetMyAssetRes;


@SpringBootTest
public class UserTest {

    @Autowired
    private UserDao userDao;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private UserController userController;

//    @Test
//    void Test1(){
//        int userIdx = 1;
//        try {
//            List<GetMyAssetRes> getMyAssetRes = userProvider.myAsset(userIdx);
////            getMyAssetRes.addAll(getMyAssetRes2);
////            getMyAssetRes.addAll(getMyAssetRes3);
//            for (int i = 0; i < getMyAssetRes.size(); i++){
//                System.out.println(getMyAssetRes.get(i).getAssetName());
//            }
//        }
//        catch(Exception e){
//            System.out.println(e.getMessage());
//        }
//
//    }

    @Test
    void 사용자_자산_구매(){
        int userIdx = 1;
        PostMyAssetReq postMyAssetReq = new PostMyAssetReq();
        postMyAssetReq.setAssetIdx(1);
        postMyAssetReq.setPrice(2000);
        postMyAssetReq.setCategory("stock");

        int result = userDao.postMyAsset(userIdx, postMyAssetReq);
        System.out.println(result);
        Assertions.assertEquals(result, 1);

        try{
//            userService.postMyAsset(userIdx, postMyAssetReq);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

}



