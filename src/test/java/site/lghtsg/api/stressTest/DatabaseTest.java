package site.lghtsg.api.stressTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.lghtsg.api.realestates.RealEstateDao;
import site.lghtsg.api.realestates.RealEstateProvider;
import site.lghtsg.api.resells.ResellDao;
import site.lghtsg.api.stocks.StockDao;
import site.lghtsg.api.users.UserDao;

@SpringBootTest
public class DatabaseTest {
//
//    static {
//        System.setProperty("com.amazonaws.sdk.disableEc2Metadata", "true");
//    }

    @Autowired
    private UserDao userDao;

    @Autowired
    private RealEstateDao realEstateDao;

    @Autowired
    private ResellDao resellDao;

    @Autowired
    private StockDao stockDao;


    @Test
    void 데이터베이스_무결성_테스트() {
        // 외래키 참조하는 데이터 빠짐 없이 들어오는지
    }

    @Test
    void 데이터베이스_최신가_업데이트_테스트(){
        // 정해진 시간마다 db에 자산의 최신가가 업데이트 되는가
    }


}
