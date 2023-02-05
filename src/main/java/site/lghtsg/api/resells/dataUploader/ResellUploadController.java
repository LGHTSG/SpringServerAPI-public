package site.lghtsg.api.resells.dataUploader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.lghtsg.api.config.BaseResponse;

@RequestMapping("resells/upload")
@RestController
public class ResellUploadController {

    @Autowired
    WebReader webReader;

    public ResellUploadController(WebReader webReader) {
        this.webReader = webReader;
    }

    @GetMapping("/info")
    public BaseResponse<String> uploadResellInfo() {
        return webReader.uploadResellInfo();
    }

    @GetMapping("/trans")
    public BaseResponse<String> uploadResellTrans(@RequestParam int startResellIdx, @RequestParam int lastResellIdx) {
        return webReader.uploadResellTrans(startResellIdx, lastResellIdx);
    }

    @GetMapping("/updateByHour")
    public BaseResponse<String> updateByHour() {
        return webReader.updateByHour();
    }

    @GetMapping("/updateByDay")
    public BaseResponse<String> updateByDay() {
        return webReader.updateByDay();
    }

}
