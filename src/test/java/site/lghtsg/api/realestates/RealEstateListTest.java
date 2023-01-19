package site.lghtsg.api.realestates;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import site.lghtsg.api.realestates.model.RealEstateTransactionData;
import site.lghtsg.api.realestates.model.upload.RealEstate;
import site.lghtsg.api.resells.ResellDao;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootTest
public class RealEstateListTest {

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
}
