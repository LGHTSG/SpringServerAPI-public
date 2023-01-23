package site.lghtsg.api.resells;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.resells.model.GetResellInfoRes;
import site.lghtsg.api.resells.model.GetResellTransactionRes;
import site.lghtsg.api.resells.model.GetResellBoxRes;

import java.util.List;

import static site.lghtsg.api.config.Constant.PARAM_DEFAULT;

@RestController
@RequestMapping("/resells")
public class ResellController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private final ResellProvider resellProvider;

    public ResellController(ResellProvider resellProvider) {
        this.resellProvider = resellProvider;
    }

    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<GetResellBoxRes>> getResellBoxes(@RequestParam(required = false) String sort, @RequestParam(required = false) String order) {
        if (sort == null)
            sort = PARAM_DEFAULT;
        if (order == null)
            order = PARAM_DEFAULT;

        try {
            List<GetResellBoxRes> getGetResellBoxesRes = resellProvider.getResellBoxes(sort, order);
            return new BaseResponse<>(getGetResellBoxesRes);
        } catch (BaseException e) {
            return new BaseResponse<>((e.getStatus()));
        }
    }

    @ResponseBody
    @GetMapping("/{resellIdx}/info")
    public BaseResponse<GetResellInfoRes> getResellInfo(@PathVariable("resellIdx") long resellIdx) {
        try {
            GetResellInfoRes getResellInfoRes = resellProvider.getResellInfo(resellIdx);
            return new BaseResponse<>(getResellInfoRes);
        } catch (BaseException e) {
            e.printStackTrace();
            return new BaseResponse<>((e.getStatus()));
        }
    }

    @ResponseBody
    @GetMapping("/{resellIdx}/prices")
    public BaseResponse<List<GetResellTransactionRes>> getResellTransaction(@PathVariable("resellIdx") long resellIdx) {
        try {
            List<GetResellTransactionRes> getResellTransactionRes = resellProvider.getResellTransaction(resellIdx);
            return new BaseResponse<>(getResellTransactionRes);
        } catch (BaseException e) {
            return new BaseResponse<>((e.getStatus()));
        }
    }
}
