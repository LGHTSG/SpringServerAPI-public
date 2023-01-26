package site.lghtsg.api.user;

import org.apache.commons.collections4.Get;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.lghtsg.api.realestates.RealEstateDao;
import site.lghtsg.api.users.UserDao;
import site.lghtsg.api.users.UserProvider;
import site.lghtsg.api.users.model.GetMyAssetRes;
//import site.lghtsg.api.users.model.GetMyAssetRes;

import java.util.List;

import static site.lghtsg.api.users.UserProvider.calculateRateOfChange;

@SpringBootTest
public class UserTest {

    @Autowired
    private UserDao userDao;
    @Autowired
    private UserProvider userProvider;

    @Test
    void Test1(){
        int userIdx = 1;
        try {
            List<GetMyAssetRes> getMyAssetRes = userProvider.myAsset(userIdx);
//            getMyAssetRes.addAll(getMyAssetRes2);
//            getMyAssetRes.addAll(getMyAssetRes3);
            for (int i = 0; i < getMyAssetRes.size(); i++){
                System.out.println(getMyAssetRes.get(i).getAssetName());
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }

    }

}



