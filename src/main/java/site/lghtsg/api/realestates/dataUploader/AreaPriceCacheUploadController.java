package site.lghtsg.api.realestates.dataUploader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponse;

@RestController
@RequestMapping("/realestates/upload")
public class AreaPriceCacheUploadController {

    @Autowired
    private AreaPriceCacheUploader areaPriceCacheUploader;


    @GetMapping("/init")
    public BaseResponse<String> initCacheTableRow(){
        try{
            areaPriceCacheUploader.initTableRow();
            return new BaseResponse<>("테이블 row insert 성공");
        }catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @GetMapping("/start")
    public BaseResponse<String> cacheAreaPrice(){
        try{
            areaPriceCacheUploader.cachePastPrice();
            return new BaseResponse<>("성공적으로 모든 지역의 누적 가격을 업데이트 하였습니다.");
        }catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }
}
