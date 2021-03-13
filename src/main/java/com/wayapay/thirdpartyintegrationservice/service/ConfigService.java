package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.config.AppConfig;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.ThirdPartyConfig;
import com.wayapay.thirdpartyintegrationservice.repo.ThirdPartyConfigRepo;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConfigService {

    private final ThirdPartyConfigRepo thirdPartyConfigRepo;
    private final AppConfig appConfig;

    public ThirdPartyNames getActiveThirdParty() throws ThirdPartyIntegrationException {
        List<ThirdPartyConfig> thirdPartyConfigs = thirdPartyConfigRepo.findAll();
        if (thirdPartyConfigs.isEmpty()){
            setActiveThirdParty(ThirdPartyNames.BAXI);
            return ThirdPartyNames.BAXI;
        }
        return thirdPartyConfigs.stream().findFirst().get().getThirdPartyName();
    }

    public void setActiveThirdParty(ThirdPartyNames thirdPartyName) throws ThirdPartyIntegrationException {
        ThirdPartyConfig thirdPartyConfig;
        Optional<ThirdPartyConfig> thirdPartyConfigOptional = thirdPartyConfigRepo.findAll().stream().findFirst();
        thirdPartyConfig = thirdPartyConfigOptional.orElseGet(ThirdPartyConfig::new);
        thirdPartyConfig.setThirdPartyName(thirdPartyName);
        try {
            thirdPartyConfigRepo.save(thirdPartyConfig);
        } catch (Exception e) {
            log.error("Unable to update the thirdpartyservice", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }
    }

    public String getThirdPartyAccountNumber() throws ThirdPartyIntegrationException {
        ThirdPartyNames activeThirdParty = getActiveThirdParty();
        switch (activeThirdParty){
            case QUICKTELLER:
                return appConfig.getQuickteller().getAccountNumber();

            case ITEX:
                return appConfig.getItex().getAccountNumber();

            case BAXI:
                return appConfig.getBaxi().getAccountNumber();

            default:
                log.error("Unable to fetch destination account number of the active third party");
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch destination account ");
        }

    }

}
