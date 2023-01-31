package site.lghtsg.api.realestates;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.lghtsg.api.config.BaseException;

import java.util.List;

import static site.lghtsg.api.config.Constant.PARAM_DEFAULT;

@SpringBootTest
public class AreaPriceCacheTest {
    @Autowired
    private RealEstateProvider realEstateProvider;
    @Test
    void 부동산_구_리스트(){
        String keyword = PARAM_DEFAULT;
        try {
            List<String> regionNames = realEstateProvider.getRegionNames(keyword);
            for(String elem : regionNames){
//                String[] tmp = elem.split(" ");
//                if(tmp.length > 2) continue;
                System.out.println(elem);
            }
        }
        catch(BaseException e){
            System.out.println(e.getStatus());
        }

    }
}
