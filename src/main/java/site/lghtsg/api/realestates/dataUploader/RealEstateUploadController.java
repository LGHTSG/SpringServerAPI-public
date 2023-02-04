package site.lghtsg.api.realestates.dataUploader;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.lghtsg.api.config.BaseResponse;

@RequestMapping("realestates/update")
@RestController
public class RealEstateUploadController {
    private final ExcelFileReader excelFileReader;
    private final RegionUploader regionUploader;

    private final ApiConnector apiConnector;

    public RealEstateUploadController(ExcelFileReader excelFileReader, RegionUploader regionUploader, ApiConnector apiConnector) {
        this.excelFileReader = excelFileReader;
        this.regionUploader = regionUploader;
        this.apiConnector = apiConnector;
    }

    /**
     * 부동산 거래 DB 업데이트 - 파일
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

    // api 데이터 불러오기 - 테스트
    @GetMapping("/connect_api")
    public String connectApi() {
        apiConnector.getData();
        return "success";
    }

}
