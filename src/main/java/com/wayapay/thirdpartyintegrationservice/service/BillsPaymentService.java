package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.service.baxi.BaxiService;
import com.wayapay.thirdpartyintegrationservice.service.interswitch.QuickTellerService;
import com.wayapay.thirdpartyintegrationservice.service.itex.ItexService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillsPaymentService {

    @Value("${active-third-party-service}")
    private String activeThirdPartyService;

    private ItexService itexService;
    private BaxiService baxiService;
    private QuickTellerService quickTellerService;

    public BillsPaymentService(ItexService itexService, BaxiService baxiService, QuickTellerService quickTellerService) {
        this.itexService = itexService;
        this.baxiService = baxiService;
        this.quickTellerService = quickTellerService;
    }

    public IThirdPartyService getBillsPaymentService(){

        if ("itex".equalsIgnoreCase(activeThirdPartyService)){
            return itexService;
        } else if ("baxi".equalsIgnoreCase(activeThirdPartyService)){
            return baxiService;
        } else {
            return quickTellerService;
        }
    }

}
