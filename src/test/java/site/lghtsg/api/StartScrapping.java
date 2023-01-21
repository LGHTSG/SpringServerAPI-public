package site.lghtsg.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import site.lghtsg.api.resells.ResellDao;

@SpringBootTest
public class StartScrapping {

    @Autowired
    private ResellDao resellDao;

    @Test
    void 스크래핑(){
      resellDao.scraping();
    }
    @Test
    void 스크래핑2(){
//        resellDao.scrapingTest();
    }
}
