package site.lghtsg.api.realestates;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import site.lghtsg.api.realestates.model.upload.RealEstate;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Set;

@SpringBootTest
public class RealEstateListTest {

    @Autowired
    private RealEstateDao realEstateDao;

    @Test
    void 부동산_리스트_반환(){

    }

    @Test
    void uploadData(){
        Set<RealEstate> realEstates = new HashSet<RealEstate>();

        realEstates.add(RealEstate.builder()
                .regionId(29170127)
                .name("test")
                .build());
        realEstates.add(RealEstate.builder()
                .regionId(29170127)
                .name("test2")
                .build());

        realEstateDao.uploadRealEstates(realEstates);
    }
}
