package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.config.AppConfig;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.repo.ThirdPartyConfigRepo;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DirtiesContext
@SpringBootTest
class ConfigServiceTest {

    @Autowired
    private ThirdPartyConfigRepo thirdPartyConfigRepo;

    @Autowired
    private AppConfig appConfig;

    private ConfigService configService;

    @BeforeEach
    void setUp() {
        configService = new ConfigService(thirdPartyConfigRepo, appConfig);
    }

    @Test
    void getActiveThirdParty() throws ThirdPartyIntegrationException {

        ThirdPartyNames activeThirdParty = configService.getActiveThirdParty();

        Assertions.assertDoesNotThrow(() -> thirdPartyConfigRepo.deleteAll());
        assertEquals(ThirdPartyNames.BAXI, configService.getActiveThirdParty());
        assertDoesNotThrow(() -> configService.getThirdPartyAccountNumber());

        configService.setActiveThirdParty(ThirdPartyNames.ITEX);
        assertEquals(ThirdPartyNames.ITEX, configService.getActiveThirdParty());
        assertDoesNotThrow(() -> configService.getThirdPartyAccountNumber());

        configService.setActiveThirdParty(ThirdPartyNames.QUICKTELLER);
        assertEquals(ThirdPartyNames.QUICKTELLER, configService.getActiveThirdParty());
        assertDoesNotThrow(() -> configService.getThirdPartyAccountNumber());

        configService.setActiveThirdParty(activeThirdParty);
        assertEquals(activeThirdParty, configService.getActiveThirdParty());
        assertDoesNotThrow(() -> configService.getThirdPartyAccountNumber());

    }
}