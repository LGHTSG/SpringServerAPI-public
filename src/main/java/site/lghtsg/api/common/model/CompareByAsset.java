package site.lghtsg.api.common.model;

import site.lghtsg.api.event.model.GetUserInfoForRank;
import site.lghtsg.api.stocks.model.StockBox;
import site.lghtsg.api.users.model.GetEventUserInfo;

import java.util.Comparator;

import static site.lghtsg.api.config.Constant.ASCENDING_PARAM;

public class CompareByAsset implements Comparator <GetUserInfoForRank>{
    private String order;

    @Override
    public int compare(GetUserInfoForRank o1, GetUserInfoForRank o2) {
        return Long.compare(o1.getUserAsset(), o2.getUserAsset()) * -1;
    }
}

