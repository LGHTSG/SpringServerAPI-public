package site.lghtsg.api.realestates;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.lghtsg.api.realestates.model.RealEstateBox;

import java.util.List;

@SpringBootTest
public class RealEstateProviderTest {

    @Autowired
    private RealEstateProvider realEstateProvider;

    @Test
    void 부동산_정렬조회(){
        String area = "서울특별시";
        String sort = "price";
        String order = "ascending";

        final int print_list_len = 10;
        List<RealEstateBox> realEstateBoxList;
        try {
            long start = System.currentTimeMillis();
            realEstateBoxList = realEstateProvider.getRealEstateBoxes(sort, order, area);

            long end = System.currentTimeMillis();

            System.out.println("Duration : " + (end - start));

            for(int i = 0; i < print_list_len; i++){
                System.out.println(realEstateBoxList.get(i).getPrice());
            }
        }
        catch(Exception e){}

    }
}
