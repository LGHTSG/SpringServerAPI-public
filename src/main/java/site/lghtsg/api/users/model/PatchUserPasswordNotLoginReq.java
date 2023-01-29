package site.lghtsg.api.users.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PatchUserPasswordNotLoginReq {
    private String email;
    private String password;
}
