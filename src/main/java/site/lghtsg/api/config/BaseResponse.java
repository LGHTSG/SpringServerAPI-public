package site.lghtsg.api.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import static site.lghtsg.api.config.BaseResponseStatus.SUCCESS;


@Getter
@AllArgsConstructor
//@JsonPropertyOrder()
// 제네릭 <T> : 외부에서 인스턴스 생성 시, 또는 정적 메소드 사용시 BaseResponse 내부의 타입을 지정해 사용할 수 있게 한다.
public class BaseResponse<T> {//BaseResponse 객체를 사용할때 성공, 실패 경우
    private Header header;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T body;

    // 요청에 성공한 경우
    public BaseResponse(T result) {
        header = new Header();
<<<<<<< HEAD
        header.setResultCode(SUCCESS.getCode());
        header.setResultMsg(SUCCESS.getMessage());
=======
        header.setCode(SUCCESS.getCode());
        header.setMessage(SUCCESS.getMessage());
>>>>>>> 73f9c84 ([FIX] resell branch merge issue hot fix process#1)
        this.body = result;
    }

    // 요청에 실패한 경우
    public BaseResponse(BaseResponseStatus status) {
        header = new Header();
<<<<<<< HEAD
        header.setResultCode(status.getCode());
        header.setResultMsg(status.getMessage());
=======
        header.setCode(status.getCode());
        header.setMessage(status.getMessage());
>>>>>>> 73f9c84 ([FIX] resell branch merge issue hot fix process#1)
    }

    @Getter
    @Setter
    public class Header{
<<<<<<< HEAD
        private String resultMsg;
        private int resultCode;
=======
        private String message;
        private int code;
>>>>>>> 73f9c84 ([FIX] resell branch merge issue hot fix process#1)
    }
}

