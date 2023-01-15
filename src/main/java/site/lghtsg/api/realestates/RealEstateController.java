package site.lghtsg.api.realestates;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.realestates.model.GetRealEstateBox;

import java.util.List;

@RestController
@RequestMapping("/realestates")
public class RealEstateController {
    private final RealEstateProvider realEstateProvider;
    private final RealEstateDao realEstateDao;

    public RealEstateController(RealEstateDao realEstateDao, RealEstateProvider realEstateProvider){
        this.realEstateProvider = realEstateProvider;
        this.realEstateDao = realEstateDao;
    }

    /**
     * @brief 부동산 리스트 조회
     * @param sort 정렬기준
     * @param order 오름차순 내림차순 여부
     * @return
     */
    @GetMapping("")
    public BaseResponse<List<GetRealEstateBox>> realEstateList(@RequestParam(required = false) String sort, @RequestParam(required = false) String order, @RequestParam(required = false) String area){
        // sort(정렬기준), order 기준에 따라 반환하는 리스트가 형성되어야 함.

        // realEstate(realEstateIdx, name, price, rateOfChange, rateCalDiff, iconImage) 반환
        // area 변수 들어오면 해당 지역에 포함되는 위치 반환
        // paging 사용을 할 생각을 왜 못했을까. 나 진짜 바본가.
        // 아니면 애초에 top 100만 짤라서 서버에서 반환하도록 통제. ok
        if(sort == null){
            // 리스트 전체 반환
        }
        if(order == null || order == "descending"){
            // 내림차순 반환
        }
        else if(order == "ascending"){

        }
    }
}
