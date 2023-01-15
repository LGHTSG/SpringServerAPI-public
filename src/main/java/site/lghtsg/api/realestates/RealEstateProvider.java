package site.lghtsg.api.realestates;

import org.springframework.stereotype.Service;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponseStatus;
import site.lghtsg.api.realestates.model.GetRealEstateBox;
import site.lghtsg.api.realestates.model.RealEstateData;

import java.util.List;

import static site.lghtsg.api.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class RealEstateProvider {

    private final RealEstateDao realEstateDao;

    public RealEstateProvider(RealEstateDao realEstateDao){
        this.realEstateDao = realEstateDao;
    }

    public List<GetRealEstateBox> getRealEstateBoxes() throws BaseException {
        try {
            List<GetRealEstateBox> getRealEstateBoxes = realEstateDao.getAllRealEstateBox();
            getRealEstateBoxes.stream().forEach(() -> );
            // 람다 써서 rateOfChange, rateCalDiff 넣어주기.
        }
        catch(Exception ignored){
            throw new BaseException(DATABASE_ERROR);
        }
    }



}
