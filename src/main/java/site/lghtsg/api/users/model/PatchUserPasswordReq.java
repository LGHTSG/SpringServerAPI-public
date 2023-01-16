package site.lghtsg.api.users.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PatchUserPasswordReq {
    private int userIdxByJwt;
    private String password;
}
