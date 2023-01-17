package site.lghtsg.api.users.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostUserReq {
    private String userName;
    private String email;
    private int emailCheck;
    private String password;
    private String profileImg;
}