package site.lghtsg.api.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.event.model.GetUserInfoForRank;
import site.lghtsg.api.users.EmailService;
import site.lghtsg.api.users.UserProvider;
import site.lghtsg.api.users.UserService;
import site.lghtsg.api.utils.JwtService;

import java.util.List;

@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    private final UserProvider userProvider;

    public EventController(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    /**
     * 이벤트 사용자 자산 랭킹 api
     */
    @GetMapping("/demoday/user-ranking")
    public BaseResponse<List<GetUserInfoForRank>> getEventUserRank(){
        List<GetUserInfoForRank> getUserInfoForRankList;
        try{
            getUserInfoForRankList = userProvider.getUserInfoForRank();
            return new BaseResponse<>(getUserInfoForRankList);
        }
        catch(BaseException be){
            return new BaseResponse<>(be.getStatus());
        }
    }
}

