package site.lghtsg.api.resells;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import site.lghtsg.api.resells.model.GetResellRes;

import java.util.List;

import static site.lghtsg.api.config.Constant.ASCENDING_PARAM;

@SpringBootTest
public class StartScrapping {
    @Autowired
    private ResellDao resellDao;

    @Test
    void 스크래핑(){
//        resellDao.scraping();
    }
    @Test
    void 리스트(){
        List<GetResellRes> resellResList = resellDao.getResellsByRate(ASCENDING_PARAM);
    }
    @Test
    void 스크래핑2(){
//        resellDao.scrapingTest();
    }
}
