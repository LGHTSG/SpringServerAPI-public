package site.lghtsg.api.realestates;

import org.springframework.web.bind.annotation.*;
import site.lghtsg.api.config.BaseException;
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
        // rateCalDiff는 데이터에 따라 결정
        // area 변수 들어오면 해당 지역에 포함되는 위치 반환
        // 1. area 변수 없는 경우
        //
        // 2. sort, order

        try{

        }
        catch(BaseException e){
             return new BaseResponse<>((e.getStatus()));
        }
    }

    /**
     * 특정 지역의 누적 가격 정보 데이터를 제공한다.
     * @param area
     * @return
     */
    @GetMapping("/prices")
    public BaseResponse<> realEstateAreaPrices(@RequestParam String area){
        try{

        }
        catch(BaseException e){
             return new BaseResponse<>((e.getStatus()));
        }
    }

    /**
     * 사용자가 선택할 수 있는 지역들의 리스트를 반환한다.
     * @return
     */
    @GetMapping("/area-relation-list")
    public BaseResponse<> areaRelationList(){
        try{

        }
        catch(BaseException e){
             return new BaseResponse<>((e.getStatus()));
        }

    }

    /**
     * 특정 부동산의 정보를 반환한다.
     * @param realestateIdx
     * @return
     */
    @GetMapping("/{realestateIdx}/info")
    public BaseResponse<> realEstateInfo(@PathVariable int realestateIdx){
        try{

        }
        catch(BaseException e){
             return new BaseResponse<>((e.getStatus()));
        }
    }

    /**
     * 특정 부동산의 누적 가격 데이터를 반환한다.
     * @param realestateIdx
     * @return
     */
    @GetMapping("/{realestateIdx}/prices")
    public BaseResponse<> realEstatePrices(@PathVariable int realestateIdx){
        try{

        }
        catch(BaseException e){
             return new BaseResponse<>((e.getStatus()));
        }
    }




}
