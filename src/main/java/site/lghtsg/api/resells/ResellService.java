package site.lghtsg.api.resells;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResellService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final site.lghtsg.api.resells.ResellDao resellDao;
    private final site.lghtsg.api.resells.ResellProvider resellProvider;

    @Autowired
    public ResellService(site.lghtsg.api.resells.ResellDao resellDao, site.lghtsg.api.resells.ResellProvider resellProvider) {
        this.resellDao = resellDao;
        this.resellProvider = resellProvider;
    }
}
