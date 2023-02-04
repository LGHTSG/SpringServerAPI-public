package site.lghtsg.api.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.utils.S3Uploader;

import java.io.IOException;

import static site.lghtsg.api.config.BaseResponseStatus.IMAGE_S3_UPLOAD_ERROR;
import static site.lghtsg.api.config.BaseResponseStatus.MISSING_REQUIRED_ARGUMENT;

@Service
public class ImageUploadService {

    @Autowired
    private S3Uploader s3Uploader;

    public String upload(MultipartFile image) throws BaseException {
        if(image.isEmpty()) throw new BaseException(MISSING_REQUIRED_ARGUMENT);
        try{
            return s3Uploader.upload(image, "/user/profileImg");
        }
        catch(IOException e){
            throw new BaseException(IMAGE_S3_UPLOAD_ERROR);
        }
    }
}
