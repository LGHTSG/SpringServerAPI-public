package site.lghtsg.api.resells;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.resells.model.GetResellRes;
import site.lghtsg.api.resells.model.GetResellTransactionRes;

import java.util.List;

@RestController
@RequestMapping("/resells")
public class ResellController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final ResellProvider resellProvider;

    @Autowired
    private final ResellService resellService;

    public ResellController(ResellProvider resellProvider, ResellService resellService) {
        this.resellProvider = resellProvider;
        this.resellService = resellService;
    }

    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<GetResellRes>> getResells(@RequestParam(required = false) String sort, @RequestParam String order) {
        try {
            if (sort == null) {
                List<GetResellRes> getResellRes = resellProvider.getResells(order);
                return new BaseResponse<>(getResellRes);
            }

            List<GetResellRes> getResellRes = resellProvider.getResellsByRate(order);
            return new BaseResponse<>(getResellRes);
        } catch (BaseException e) {
            return new BaseResponse<>((e.getStatus()));
        }
    }

    @ResponseBody
    @GetMapping("/{resellIdx}/info")
    public BaseResponse<GetResellRes> getResell(@PathVariable("resellIdx") int resellIdx) {
        try {
            GetResellRes getResellRes = resellProvider.getResell(resellIdx);
            return new BaseResponse<>(getResellRes);
        } catch (BaseException e) {
            e.printStackTrace();
            return new BaseResponse<>((e.getStatus()));
        }
    }

    @ResponseBody
    @GetMapping("/{resellIdx}/prices")
    public BaseResponse<List<GetResellTransactionRes>> getResellTransaction(@PathVariable("resellIdx") int resellIdx) {
        try {
            List<GetResellTransactionRes> getResellTransactionRes = resellProvider.getResellTransaction(resellIdx);
            return new BaseResponse<>(getResellTransactionRes);
        } catch (BaseException e) {
            return new BaseResponse<>((e.getStatus()));
        }
    }
}
