package site.lghtsg.api.config;

// 프로젝트에서 공통적으로 사용하는 상수들
public class Constant {
    // 예시 public static final String IP_ADDRESS = "127.0.0.1";
    public static String ASSET_CATEGORY_STOCK = "stock";
    public static String ASSET_CATEGORY_REALESTATE = "realestate";
    public static String ASSET_CATEGORY_RESELL = "resell";

    public static String PARAM_DEFAULT = "default";
    public static String ASCENDING_PARAM = "ascending";
    public static String DESCENDING_PARAM = "descending";
    public static String SORT_FLUCTUATION_PARAM = "fluctuation";
    public static String SORT_PRICE_PARAM = "price";
    public static String SORT_TRADING_VOL_PARAM = "trading-volume";
    public static String SORT_MARKET_CAP_PARAM = "market-cap";
    public static String LIST_LIMIT_QUERY = "LIMIT 100";

    public static int MILLISECONDS = 1000;
    public static int SECONDS = 60;
    public static int MINUTES = 60;
    public static int HOURS = 24;
    public static int DAYS = 30;
    public static int LIST_LIMIT = 200;

    public static String WITHIN_3_MONTHS = "3개월 이내";
    public static String WITHIN_6_MONTHS = "6개월 이내";
    public static String WITHIN_1_YEAR = "1년 이내";
    public static String MORE_THAN_1_YEAR= "1년 이상";

    public static String SINGLE_TRANSACTION_HISTORY = "변동 정보 없음";
    public static String DEFAULT_PROFILE_IMG_URL= "https://lghtsgs3imageuploader.s3.ap-northeast-2.amazonaws.com/%EA%B8%B0%EB%B3%B8%EC%9D%B4%EB%AF%B8%EC%A7%80/default-proImg.jpg";

}
