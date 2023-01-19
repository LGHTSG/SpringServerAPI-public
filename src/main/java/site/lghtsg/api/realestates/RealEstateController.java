package site.lghtsg.api.realestates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.realestates.dataUploader.ApiConnector;
import site.lghtsg.api.realestates.model.RealEstateBox;
import site.lghtsg.api.realestates.model.RealEstateInfo;
import site.lghtsg.api.realestates.model.RealEstateTransactionData;

import java.util.Collections;
import java.util.List;

import static site.lghtsg.api.config.BaseResponseStatus.GET_REGIONS_EMPTY_KEYWORD;

@RestController
@RequestMapping("/realestates")
public class RealEstateController {
    private final RealEstateProvider realEstateProvider;
    private final ApiConnector apiConnector;
    @Autowired
    private RealEstateDao realEstateDao;

    public RealEstateController(RealEstateProvider realEstateProvider, ApiConnector apiConnector){
        this.realEstateProvider = realEstateProvider;
        this.apiConnector = apiConnector;
    }

    /**
     * TODO : 1. 정렬기준 : fluctuation(필수, 작업중), price(옵션, 시작 전)
     * TODO : 2. 오름차순, 내림차순 적용
     * @brief 부동산 리스트 조회
     * @param sort 정렬기준
     * @param order 오름차순 내림차순 여부
     * @return
     */
    @GetMapping("")
    public BaseResponse<List<RealEstateBox>> realEstateList(@RequestParam(required = false) String sort, @RequestParam(required = false) String order, @RequestParam(required = false) String area){
        try{
            List<RealEstateBox> realEstateBoxes = realEstateProvider.getRealEstateBoxes(sort, order, area);
            return new BaseResponse<>(realEstateBoxes);
        }
        catch(BaseException e){
             return new BaseResponse<>((e.getStatus()));
        }
    }
    @GetMapping("test")
    public void listsort(){
        long start = System.currentTimeMillis();
        System.out.println(start);
        List<RealEstateTransactionData> realEstateTransactionData = realEstateDao.getAllTransactionData();
//        Collections.sort(realEstateTransactionData);
        long end = System.currentTimeMillis();
        System.out.println(end);
        System.out.println("duration : " + (end - start));
    }

    /**
     * TODO : 1. Dao 단계에서 하루 단위 중복 거래 등 데이터 처리 필요
     * TODO : 2. 특정 지역의 가격 추세를 확인할 수 있는 그래프를 제공하는 것이 목적.
     *          따라서 같은 지역이라도 부동산간의 가격 차이가 크기에 같은 날 거래된 부동산들의 평균가를 제시하는 방향 고려중
     * 특정 지역의 누적 가격 정보 데이터를 제공한다.
     * @param area
     * @return
     */
    @GetMapping("/prices")
    public BaseResponse<List<RealEstateTransactionData>> realEstateAreaPrices(@RequestParam String area){
        try{
            List<RealEstateTransactionData> realEstateTransactionData = realEstateProvider.getRealEstatePricesInArea(area);
            return new BaseResponse<>(realEstateTransactionData);
        }
        catch(BaseException e){
             return new BaseResponse<>((e.getStatus()));
        }
    }

    /**
     * 검색어를 포함하는 지역들의 리스트를 반환한다.
     * @param keyword 검색어
     * @return areas 지역 리스트
     */
    @GetMapping("/area-relation-list")
    public BaseResponse<List<String>> areaRelationList(@RequestParam(required = false) String keyword){
        try {
            List<String> areaRelationList = realEstateProvider.getRegionNames(keyword);
            return new BaseResponse<>(areaRelationList);
        } catch(BaseException e) {
            return new BaseResponse<>((e.getStatus()));
        }
    }

    /**
     * TODO : 1. Info와 Box 관계 명확해지면 Provider 이하 리팩토링
     * 특정 부동산의 정보를 반환한다.
     * @param realestateIdx
     * @return
     */
    @GetMapping("/{realestateIdx}/info")
    public BaseResponse<RealEstateInfo> realEstateInfo(@PathVariable long realestateIdx){
        try{
            RealEstateInfo realEstateInfo = realEstateProvider.getRealEstateInfo(realestateIdx);
            return new BaseResponse<>(realEstateInfo);
        }
        catch(BaseException e){
             return new BaseResponse<>((e.getStatus()));
        }
    }

    /**
     * TODO : 1. Dao 단계에서 하루 단위 중복 거래 등 데이터 처리 필요
     * 특정 부동산의 누적 가격 데이터를 반환한다.
     * @param realEstateIdx
     * @return
     */
    @GetMapping("/{realEstateIdx}/prices")
    public BaseResponse<List<RealEstateTransactionData>> realEstatePrices(@PathVariable long realEstateIdx){
        try{
            List<RealEstateTransactionData> realEstateTransactionData = realEstateProvider.getRealEstatePrices(realEstateIdx);
            return new BaseResponse<>(realEstateTransactionData);
        }
        catch(BaseException e){
             return new BaseResponse<>((e.getStatus()));
        }
    }

    /**
     * 부동산 거래 DB 업데이트
     * @return
     */
    @GetMapping("/connect_api")
    public BaseResponse<String> updateData() {
        return apiConnector.getData();
    }

}
