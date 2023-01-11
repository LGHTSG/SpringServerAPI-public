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
    REQUEST_ERROR(2000, "입력값을 확인해주세요."),
    EMPTY_JWT( 2001, "JWT를 입력해주세요."),
    INVALID_JWT( 2002, "유효하지 않은 JWT입니다."),
    INVALID_USER_JWT(2003,"권한이 없는 유저의 접근입니다."),
    INVALID_OWNER_JWT(2004,"권한이 없는 유저의 접근입니다.(개인의 가게만 생성, 수정 삭제 가능)"),

    // users
    USERS_EMPTY_USER_ID( 2010, "유저 아이디 값을 확인해주세요."),

    // [POST] /users
    POST_USERS_EMPTY_EMAIL(2015, "이메일을 입력해주세요."),
    POST_USERS_INVALID_EMAIL( 2016, "이메일 형식을 확인해주세요."),
    POST_USERS_EXISTS_EMAIL(2017,"중복된 이메일입니다."),
    POST_USERS_EMPTY_PASSWORD( 2020, "비밀번호를 입력해주세요."),
    POST_USERS_INVALID_PASSWORD( 2021, "비밀번호 형식을 확인해주세요."),

    POST_USERS_EMPTY_PHONENUM( 2025, "전화번호를 입력해주세요."),
    POST_USERS_INVALID_PHONENUM( 2026, "전화번호 형식을 확인해주세요."),
    POST_USERS_EXISTS_PHONENUM(2027,"중복된 전화번호입니다."),


    /**
     * 3000 : Response 오류
     */
    // Common
    RESPONSE_ERROR(3000, "값을 불러오는데 실패하였습니다."),

    // [POST] /users
    DUPLICATED_EMAIL( 3013, "중복된 이메일입니다."),
    FAILED_TO_LOGIN(3015,"없는 아이디거나 비밀번호가 틀렸습니다."),


    /**
     * 4000 : Database, Server 오류
     */
    DATABASE_ERROR( 4000, "데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR( 4001, "서버와의 연결에 실패하였습니다."),

    //[PATCH] /users/{userIdx}
    MODIFY_FAIL_USERNAME(4014,"유저네임 수정 실패"),
    MODIFY_FAIL_USERPHONENUM(4015,"유저 전화번호 수정 실패"),
    MODIFY_FAIL_MENU(4020,"메뉴 수정 실패"),
    MODIFY_FAIL_RESTAURANT(4022,"가게 수정 실패"),

    // 삭제
    DELETE_FAIL_MENU(4030,"메뉴 삭제 실패"),
    DELETE_FAIL_RESTAURANT(4032,"가게 삭제 실패"),

    PASSWORD_ENCRYPTION_ERROR( 4011, "비밀번호 암호화에 실패하였습니다."),
    PASSWORD_DECRYPTION_ERROR( 4012, "비밀번호 복호화에 실패하였습니다.");


    // 5000 : 필요시 만들어서 쓰세요
    // 6000 : 필요시 만들어서 쓰세요


    private final int code;
    private final String message;

    private BaseResponseStatus(int code, String message) { //BaseResponseStatus 에서 각 해당하는 코드를 생성자로 맵핑
        this.code = code;
        this.message = message;
    }
}
