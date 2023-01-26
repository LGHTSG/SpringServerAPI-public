package site.lghtsg.api.common;

import site.lghtsg.api.realestates.model.RealEstateBox;
import site.lghtsg.api.resells.model.GetResellBoxRes;
import site.lghtsg.api.stocks.model.StockBox;
import site.lghtsg.api.users.model.GetMyAssetRes;

import java.util.ArrayList;
import java.util.List;

public class Wrapper {
//    public List<GetMyAssetRes> boxToAssetWrapper(List<T> realEstateBoxes){
//        List<GetMyAssetRes> getMyAssetResList;
//        for(int i = 0; i < realEstateBoxes.size(); i++){
//            GetMyAssetRes getMyAssetRes = new GetMyAssetRes();
//            getMyAssetRes.setUpdatedAt(realEstateBoxes.get(i).getUpdatedAt());
//        }
//    }

    public List<GetMyAssetRes> realEstateBoxToAssetWrapper(List<RealEstateBox> realEstateBoxes){
        List<GetMyAssetRes> getMyAssetResList = new ArrayList<>();
        for(int i = 0; i < realEstateBoxes.size(); i++){
            GetMyAssetRes getMyAssetRes = new GetMyAssetRes();
            getMyAssetRes.setAssetName(realEstateBoxes.get(i).getName());
            getMyAssetRes.setPrice(realEstateBoxes.get(i).getPrice());
            getMyAssetRes.setIconImage(realEstateBoxes.get(i).getIconImage());
            getMyAssetRes.setSaleCheck(realEstateBoxes.get(i).getSaleCheck());
            getMyAssetRes.setRateOfChange(realEstateBoxes.get(i).getRateOfChange());
            getMyAssetRes.setRateCalDateDiff(realEstateBoxes.get(i).getRateCalDateDiff());
            getMyAssetRes.setUpdatedAt(realEstateBoxes.get(i).getUpdatedAt());
            getMyAssetResList.add(getMyAssetRes);
        }
        return getMyAssetResList;
    }
    public List<GetMyAssetRes> resellBoxToAssertWrapper(List<GetResellBoxRes> resellBoxes){
        List<GetMyAssetRes> getMyAssetResList = new ArrayList<>();
        for(int i = 0; i < resellBoxes.size(); i++){
            GetMyAssetRes getMyAssetRes = new GetMyAssetRes();
            getMyAssetRes.setAssetName(resellBoxes.get(i).getName());
            getMyAssetRes.setPrice(resellBoxes.get(i).getPrice());
            getMyAssetRes.setIconImage(resellBoxes.get(i).getIconImage());
            getMyAssetRes.setSaleCheck(resellBoxes.get(i).getSaleCheck());
            getMyAssetRes.setRateOfChange(resellBoxes.get(i).getRateOfChange());
            getMyAssetRes.setRateCalDateDiff(resellBoxes.get(i).getRateCalDateDiff());
            getMyAssetRes.setUpdatedAt(resellBoxes.get(i).getUpdatedAt());
            getMyAssetResList.add(getMyAssetRes);
        }
        return getMyAssetResList;
    }
    public List<GetMyAssetRes> stockBoxToAssertWrapper(List<StockBox> stockBoxes){
        List<GetMyAssetRes> getMyAssetResList = new ArrayList<>();
        for(int i = 0; i < stockBoxes.size(); i++){
            GetMyAssetRes getMyAssetRes = new GetMyAssetRes();
            getMyAssetRes.setAssetName(stockBoxes.get(i).getName());
            getMyAssetRes.setPrice(stockBoxes.get(i).getPrice());
            getMyAssetRes.setIconImage(stockBoxes.get(i).getIconImage());
            getMyAssetRes.setSaleCheck(stockBoxes.get(i).getSaleCheck());
            getMyAssetRes.setRateOfChange(stockBoxes.get(i).getRateOfChange());
            getMyAssetRes.setRateCalDateDiff(stockBoxes.get(i).getRateCalDateDiff());
            getMyAssetRes.setUpdatedAt(stockBoxes.get(i).getUpdatedAt());
            getMyAssetResList.add(getMyAssetRes);
        }
        return getMyAssetResList;
    }


}
