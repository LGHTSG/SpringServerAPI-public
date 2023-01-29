package site.lghtsg.api.stressTest;

import org.springframework.beans.factory.annotation.Autowired;
import site.lghtsg.api.realestates.RealEstateProvider;
import site.lghtsg.api.resells.ResellProvider;
import site.lghtsg.api.stocks.StockProvider;
import site.lghtsg.api.users.UserProvider;

public class SpeedTest {

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private RealEstateProvider realEstateProvider;

    @Autowired
    private ResellProvider resellProvider;

    @Autowired
    private StockProvider stockProvider;
    
     
    
}
