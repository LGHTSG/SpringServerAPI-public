package site.lghtsg.api.config;

import lombok.Getter;

/**
 * 에러 코드 관리
 */
@Getter
public enum BaseResponseStatus {
    /**
     * 1000 : 요청 성공
     */
    SUCCESS(1000, "요청에 성공하였습니다."),

    /**
     * TODO : 필독! 아래의 번호대를 지켜서 코드를 지정해주세요
     * x0xx : user api & 공통(DB 연결 에러 등)
     * x1xx : stock api
     * x2xx : realEstate api
     * x3xx : resell api
     */

    /**
     * 2000 : Request 오류
     */

    // Common
    EMPTY_JWT(2000, "JWT 토큰을 입력해주세요."),

    // [POST] /users
    // 회원가입
    EMPTY_NAME( 2001, "이름을 입력해주세요."),
    EMPTY_EMAIL( 2002, "이메일을 입력해주세요."),
    INVALID_EMAIL( 2003, "이메일 형식을 확인해주세요."),
    EXISTS_EMAIL(2004,"중복된 이메일입니다."),
    FAIL_VERIFICATION_EMAIL(2005,"이메일 인증이 되지 않았습니다."),
    EMPTY_PASSWORD( 2006, "비밀번호를 입력해주세요."),
    INVALID_PASSWORD( 2007, "비밀번호 형식을 확인해주세요."),
    NOT_AGREE_TERMS(2008,"약관 동의를 하지 않았습니다."),

    // [PATCH] /users/changeInfo
    // 회원정보 수정
    NOT_MATCH_PASTPASSWORD(2020,"이전 비밀번호가 일치하지 않습니다."),
    NOT_MATCH_PASSWORD(2021,"비밀번호가 일치하지 않습니다."),

    // Asset
    NOT_EXIST_ASSET(2030, "해당 자산을 보유하지 않았습니다."),

    // [POST] /users/log-in
    WITHDRAW_USER(2031,"존재하지 않거나 탈퇴한 유저입니다."),

    // [GET] /realestates/area-realation-list
    GET_REGIONS_EMPTY_KEYWORD(2201, "검색어를 입력해주세요."),


    /**
     * 3000 : Response 오류
     */
    // Common
    RESPONSE_ERROR(3000, "값을 불러오는데 실패하였습니다."),

    NOT_EXISTING_EMAIL(3010,"존재하지 않는 이메일입니다."),
    // [POST] /users
    // DUPLICATED_EMAIL( 3013, "중복된 이메일입니다."),
    FAILED_TO_LOGIN(3015," 없는 아이디이거나 비밀번호가 틀렸습니다."),


    /**
     * 4000 : Database, Server 오류
     */
    DATABASE_ERROR( 4000, "데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR( 4001, "서버와의 연결에 실패하였습니다."),

    //[PATCH] /users/changeInfo
    MODIFY_FAIL_PASSWORD(4002,"비밀번호 변경 실패"),
    MODIFY_FAIL_PROFILEIMAGE(4003,"프로필 사진 변경 실패"),
    DELETE_FAIL_USER(4004,"회원 탈퇴 실패"),

    // [POST] 자산 구매 및 판매
    PURCHASE_FAIL_ASSET(4005,"자산 구매 실패"),
    SELL_FAIL_ASSET(4006,"자산 판매 실패"),
    DELETE_FAIL_ASSET_LIST(4007,"자산 리스트 삭제 실패"),
    FAIL_TO_INSERT_SALES(4008,"수익율 기록 실패"),
    SELL_AHEAD_OF_PREVIOUS_PURCHACE(4009,"판매하려는 시기 이후에 구매하였습니다."),
    WRONG_PARAMETER_INPUT(4010,"잘못된 변수 입력입니다"),
    USER_TRANSACTION_DATA_ERROR(4015,"transactionStatus == 1이 2개 이상입니다."),
    NO_PREVIOUS_USER_TRANSACTION(4016,"해당 자산에 대해 사용자가 거래한 적이 없습니다"),

    // 삭제

    PASSWORD_ENCRYPTION_ERROR( 4013, "비밀번호 암호화에 실패하였습니다."),
    PASSWORD_DECRYPTION_ERROR( 4014, "비밀번호 복호화에 실패하였습니다."),
    JWT_VALIDATE_ERROR(4015,"JWT validation 에러"),
    JWT_ERROR(4016,"JWT 에러"),

    IMAGE_S3_UPLOAD_ERROR( 4020, "이미지를 S3 버킷 업로드에 실패하였습니다."),
    LOGOUT_REDIS_SERVICE_ERROR( 4021, "로그아웃에서 에러가 발생했습니다."),

    // 42XX
    DATALIST_SORTING_ERROR(4201, "데이터 정렬 과정에서 오류가 발생했습니다."),
    DATALIST_CAL_RATE_ERROR(4202, "증감율 계산 과정에서 오류가 발생했습니다."),
    MISSING_REQUIRED_ARGUMENT(4203, "필요한 변수가 입력되지 않았습니다"),
    INCORRECT_REQUIRED_ARGUMENT(4204, "잘못된 변수가 입력되었습니다."),

    REQUESTED_DATA_FAIL_TO_EXIST(4205, "요청한 데이터가 존재하지 않습니다."),
    RETURN_EXCEEDING_REQUESTED(4206, "반환하는 데이터가 요청값을 초과합니다.(1개 자산 요청 -> 2개 이상 반환)"),
    FILE_SAVE_ERROR(4207, "저장할 파일을 생성하다 에러가 발생했습니다."),
    FILE_READ_ERROR(4207, "파일을 읽어오는 과정에서 에러가 발생했습니다."),

    EVENT_ERROR_DUPLICATE_PURCHASE(4208, "동일 자산을 2번 이상 구매하셨습니다.");
    // 5000 : 필요시 만들어서 쓰세요
    // 6000 : 필요시 만들어서 쓰세요


    private final int code;
    private final String message;

    private BaseResponseStatus(int code, String message) { //BaseResponseStatus 에서 각 해당하는 코드를 생성자로 맵핑
        this.code = code;
        this.message = message;
    }
}
