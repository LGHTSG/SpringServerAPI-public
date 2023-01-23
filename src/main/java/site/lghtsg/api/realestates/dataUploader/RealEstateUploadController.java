package site.lghtsg.api.realestates.dataUploader;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.lghtsg.api.config.BaseResponse;

@RequestMapping("realestates/update")
@RestController
public class RealEstateUploadController {

    private final ApiConnector apiConnector;
    private final ExcelFileReader excelFileReader;
    private final RegionUploader regionUploader;

    public RealEstateUploadController(ApiConnector apiConnector, ExcelFileReader excelFileReader, RegionUploader regionUploader) {
        this.apiConnector = apiConnector;
        this.excelFileReader = excelFileReader;
        this.regionUploader = regionUploader;
    }

    /**
     * @brief 부동산 거래 DB 업데이트 - api
     */
    @GetMapping("/connect_api")
    public BaseResponse<String> updateData() {
        return apiConnector.getData();
    }


    /**
     * @brief 부동산 거래 DB 업데이트 - 파일
     */
    @GetMapping("/upload_file_data")
    public BaseResponse<String> uploadFileData() {
        return excelFileReader.readData();
    }

    /**
     * 지역 정보 업로드
     */
    @GetMapping("/upload_region")
    public String uploadRegion() {
        return regionUploader.readData();

    }

    @GetMapping("/last_transactions")
    public BaseResponse updateLastTransactions() {
        try {
            apiConnector.updateLastTransactions();
            return new BaseResponse<>("거래 업데이트 성공");
        } catch (Exception e) {
            return new BaseResponse<>("거래 업데이트 실패");
        }
    }
}
