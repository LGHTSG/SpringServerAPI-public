package site.lghtsg.api.resells;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.lghtsg.api.resells.dataUploader.WebReader;
import site.lghtsg.api.resells.model.GetResellBoxRes;

import java.util.List;

@SpringBootTest
public class StartScrapping {
    @Autowired
    private ResellDao resellDao;
    @Autowired
    private WebReader webReader;

    @Test
    void 스크래핑(){
        webReader.scraping();
    }

    @Test
    void 리스트(){
        List<GetResellBoxRes> resellResList = resellDao.getResellBoxes();
    }
}
