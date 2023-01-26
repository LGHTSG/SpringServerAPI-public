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
    WITHDRAW_USER(2030,"존재하지 않거나 탈퇴한 유저입니다."),

    // [GET] /realestates/area-realation-list
    GET_REGIONS_EMPTY_KEYWORD(2201, "검색어를 입력해주세요."),


    /**
     * 3000 : Response 오류
     */
    // Common
    RESPONSE_ERROR(3000, "값을 불러오는데 실패하였습니다."),

    // [POST] /users
    // DUPLICATED_EMAIL( 3013, "중복된 이메일입니다."),
    FAILED_TO_LOGIN(3015,"없는 아이디거나 비밀번호가 틀렸습니다."),


    /**
     * 4000 : Database, Server 오류
     */
    DATABASE_ERROR( 4000, "데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR( 4001, "서버와의 연결에 실패하였습니다."),

    //[PATCH] /users/changeInfo
    MODIFY_FAIL_PASSWORD(4002,"비밀번호 변경 실패"),
    MODIFY_FAIL_PROFILEIMAGE(4003,"프로필 사진 변경 실패"),
    DELETE_FAIL_USER(4004,"회원 탈퇴 실패"),
    PURCHASE_FAIL_ASSET(4005,"자산 구매 실패"),
    SALE_FAIL_ASSET(4006,"자산 판매 실패"),
    DELETE_FAIL_ASSET_LIST(4007,"자산 리스트 삭제 실패"),


    // 삭제

    PASSWORD_ENCRYPTION_ERROR( 4011, "비밀번호 암호화에 실패하였습니다."),
    PASSWORD_DECRYPTION_ERROR( 4012, "비밀번호 복호화에 실패하였습니다."),

    // 42XX
    DATALIST_SORTING_ERROR(4201, "데이터 정렬 과정에서 오류가 발생했습니다."),
    DATALIST_CAL_RATE_ERROR(4202, "증감율 계산 과정에서 오류가 발생했습니다."),
    MISSING_REQUIRED_ARGUMENT(4203, "필요한 변수가 입력되지 않았습니다"),
    INCORRECT_REQUIRED_ARGUMENT(4204, "잘못된 변수가 입력되었습니다."),

    REQUESTED_DATA_FAIL_TO_EXIST(4205, "요청한 데이터가 존재하지 않습니다.");
    // 5000 : 필요시 만들어서 쓰세요
    // 6000 : 필요시 만들어서 쓰세요


    private final int code;
    private final String message;

    private BaseResponseStatus(int code, String message) { //BaseResponseStatus 에서 각 해당하는 코드를 생성자로 맵핑
        this.code = code;
        this.message = message;
    }
}
