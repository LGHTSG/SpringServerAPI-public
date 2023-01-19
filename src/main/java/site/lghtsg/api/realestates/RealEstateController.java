package site.lghtsg.api.realestates;

import org.springframework.web.bind.annotation.*;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.realestates.dataUploader.ApiConnector;
import site.lghtsg.api.realestates.dataUploader.ExcelFileReader;
import site.lghtsg.api.realestates.model.RealEstateBox;
import site.lghtsg.api.realestates.model.RealEstateInfo;
import site.lghtsg.api.realestates.model.RealEstateTransactionData;

import java.util.List;

import static site.lghtsg.api.config.BaseResponseStatus.GET_REGIONS_EMPTY_KEYWORD;

@RestController
@RequestMapping("/realestates")
public class RealEstateController {
    private final RealEstateProvider realEstateProvider;
    private final RealEstateDao realEstateDao;
    private final ApiConnector apiConnector;
    private final ExcelFileReader excelFileReader;

    public RealEstateController(RealEstateDao realEstateDao, RealEstateProvider realEstateProvider, ApiConnector apiConnector, ExcelFileReader excelFileReader){
        this.realEstateProvider = realEstateProvider;
        this.realEstateDao = realEstateDao;
        this.apiConnector = apiConnector;
        this.excelFileReader = excelFileReader;
    }

    /**
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

    /**
     * 특정 지역의 누적 가격 정보 데이터를 제공한다.
     * @param area
     * @return
     */
    @GetMapping("/prices")
    public BaseResponse<List<RealEstateTransactionData>> realEstateAreaPrices(@RequestParam String area){
//        try{
//
//        }
//        catch(BaseException e){
//             return new BaseResponse<>((e.getStatus()));
//        }
        return null;
    }

    /**
     * 검색어를 포함하는 지역들의 리스트를 반환한다.
     * @param keyword 검색어
     * @return areas 지역 리스트
     */
    @GetMapping("/area-relation-list")
    public BaseResponse<List<String>> areaRelationList(String keyword){
        if (keyword.isBlank()) return new BaseResponse<>(GET_REGIONS_EMPTY_KEYWORD);

        try {
            return realEstateProvider.getRegionNames(keyword);
        } catch(BaseException e) {
            return new BaseResponse<>((e.getStatus()));
        }
    }

    /**
     * 특정 부동산의 정보를 반환한다.
     * @param realestateIdx
     * @return
     */
    @GetMapping("/{realestateIdx}/info")
    public BaseResponse<RealEstateInfo> realEstateInfo(@PathVariable int realestateIdx){
//        try{
//
//        }
//        catch(BaseException e){
//             return new BaseResponse<>((e.getStatus()));
//        }
        return null;
    }

    /**
     * 특정 부동산의 누적 가격 데이터를 반환한다.
     * @param realestateIdx
     * @return
     */
    @GetMapping("/{realestateIdx}/prices")
    public BaseResponse<List<RealEstateTransactionData>> realEstatePrices(@PathVariable int realestateIdx){
//        try{
//
//        }
//        catch(BaseException e){
//             return new BaseResponse<>((e.getStatus()));
//        }
        return null;
    }

    /**
     * 부동산 거래 DB 업데이트 - api
     * @return
     */
    @GetMapping("/connect_api")
    public BaseResponse<String> updateData() {
        return apiConnector.getData();
    }

    /**
     * 부동산 거래 DB 업데이트 - 파일
     * @return
     */
    @GetMapping("/upload_file_data")
    public BaseResponse<String> uploadFileData() {
        return excelFileReader.readData();
    }





}
