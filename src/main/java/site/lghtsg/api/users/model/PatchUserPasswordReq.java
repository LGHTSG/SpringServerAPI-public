package site.lghtsg.api.users.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PatchUserPasswordReq {
    private String pastPassword;
    private String password;
    private int userIdx;
}
