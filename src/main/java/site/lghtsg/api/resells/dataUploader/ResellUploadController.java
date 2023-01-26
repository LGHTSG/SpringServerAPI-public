package site.lghtsg.api.resells.dataUploader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/new")
    public BaseResponse<String> uploadResellDate() {
        return webReader.scraping();
    }

}
