package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.BillerConsumerFeeRequest;
import com.wayapay.thirdpartyintegrationservice.dto.BillerConsumerFeeResponse;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.BillerConsumerFee;
import com.wayapay.thirdpartyintegrationservice.repo.BillerConsumerFeeRepo;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import com.wayapay.thirdpartyintegrationservice.util.FeeBearer;
import com.wayapay.thirdpartyintegrationservice.util.FeeType;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext
@SpringBootTest
class BillerConsumerFeeServiceTest {

    @Autowired
    private BillerConsumerFeeRepo billerConsumerFeeRepo;

    private BillerConsumerFeeService billerConsumerFeeService;

    private static final String BILLER_NAME = "testBillerName";
    private static final String BILLER_NAME_2 = "testBillerName2";

    @BeforeEach
    void setUp() {
        billerConsumerFeeService = new BillerConsumerFeeService(billerConsumerFeeRepo);

        BillerConsumerFee billerConsumerFee = billerConsumerFeeRepo.findByBiller(BILLER_NAME);
        if (!Objects.isNull(billerConsumerFee)){
            billerConsumerFeeRepo.delete(billerConsumerFee);
        }

        billerConsumerFee = billerConsumerFeeRepo.findByBiller(BILLER_NAME_2);
        if (!Objects.isNull(billerConsumerFee)){
            billerConsumerFeeRepo.delete(billerConsumerFee);
        }
    }

    @Test
    void testCreateUpdateGetAllGetByIdAndToggle() throws ThirdPartyIntegrationException {

        BillerConsumerFeeRequest billerConsumerFeeRequest = new BillerConsumerFeeRequest();
        billerConsumerFeeRequest.setBiller(BILLER_NAME);
        billerConsumerFeeRequest.setFeeBearer(FeeBearer.CONSUMER.name());
        billerConsumerFeeRequest.setFeeType(FeeType.FIXED.toString());
        billerConsumerFeeRequest.setMaxFixedValueWhenPercentage(new BigDecimal("10.00"));
        billerConsumerFeeRequest.setThirdPartyName(ThirdPartyNames.BAXI.toString());
        billerConsumerFeeRequest.setValue(new BigDecimal("1.00"));

        //create
        ResponseHelper responseHelper = billerConsumerFeeService.create(billerConsumerFeeRequest);
        assertNotNull(responseHelper.getData(), () -> fail("responseHelper.getData() is null"));
        BillerConsumerFeeResponse billerConsumerFeeResponse = CommonUtils.getObjectMapper().convertValue(responseHelper.getData(), BillerConsumerFeeResponse.class);
        assertNotNull(billerConsumerFeeResponse);
        assertNotNull(billerConsumerFeeResponse.getId());

        //create another biller for update purpose
        billerConsumerFeeRequest.setBiller(BILLER_NAME_2);
        assertNotNull(billerConsumerFeeService.create(billerConsumerFeeRequest));

        //Creating what has been created
        assertThrows(ThirdPartyIntegrationException.class, () -> billerConsumerFeeService.create(billerConsumerFeeRequest));

        //getFee when it is fixed
        BigDecimal fixedFee = billerConsumerFeeService.getFee(BigDecimal.valueOf(1000), ThirdPartyNames.valueOf(billerConsumerFeeRequest.getThirdPartyName()), BILLER_NAME);
        assertEquals(billerConsumerFeeRequest.getValue(), fixedFee);

        //update
        //updating without Id
        assertThrows(ThirdPartyIntegrationException.class, () -> billerConsumerFeeService.update(billerConsumerFeeRequest));

        //set the id
        billerConsumerFeeRequest.setId(billerConsumerFeeResponse.getId());
        assertThrows(ThirdPartyIntegrationException.class, () -> billerConsumerFeeService.update(billerConsumerFeeRequest));

        billerConsumerFeeRequest.setBiller(BILLER_NAME);
        billerConsumerFeeRequest.setValue(BigDecimal.TEN);
        billerConsumerFeeRequest.setFeeType(FeeType.PERCENTAGE.toString());

        ResponseHelper updateResponseHelper = billerConsumerFeeService.update(billerConsumerFeeRequest);
        assertNotNull(updateResponseHelper);
        BillerConsumerFeeResponse updateBillerConsumerFeeResponse = CommonUtils.getObjectMapper().convertValue(updateResponseHelper.getData(), BillerConsumerFeeResponse.class);
        assertNotNull(updateBillerConsumerFeeResponse);
        assertEquals(BigDecimal.TEN, updateBillerConsumerFeeResponse.getValue());

        //getFee when it is percentage and MaxPercentageValue is exceeded
        BigDecimal percentageFee = billerConsumerFeeService.getFee(BigDecimal.valueOf(1000), ThirdPartyNames.valueOf(billerConsumerFeeRequest.getThirdPartyName()), BILLER_NAME);
        assertEquals(new BigDecimal("10.00"), percentageFee);

        billerConsumerFeeRequest.setMaxFixedValueWhenPercentage(BigDecimal.valueOf(20000));
        updateResponseHelper = billerConsumerFeeService.update(billerConsumerFeeRequest);
        assertNotNull(updateResponseHelper);

        //getFee when it is percentage and MaxPercentageValue is NOT exceeded
        percentageFee = billerConsumerFeeService.getFee(BigDecimal.valueOf(1000), ThirdPartyNames.valueOf(billerConsumerFeeRequest.getThirdPartyName()), BILLER_NAME);
        assertEquals(new BigDecimal("100.00"), percentageFee);

        //getFee when no setting found for the provided biller
        BigDecimal billerFeeSettingNotFound = billerConsumerFeeService.getFee(BigDecimal.valueOf(1000), ThirdPartyNames.valueOf(billerConsumerFeeRequest.getThirdPartyName()), "testetttt");
        assertEquals(BigDecimal.ZERO, billerFeeSettingNotFound);

        //getAll
        ResponseHelper getAllResponseHelper = billerConsumerFeeService.getAll();
        assertNotNull(getAllResponseHelper.getData());

        //getById
        ResponseHelper getByIdResponseHelper = billerConsumerFeeService.getById(billerConsumerFeeRequest.getId());
        assertNotNull(getByIdResponseHelper.getData());
        BillerConsumerFeeResponse getByIdBillerConsumerFeeResponse = CommonUtils.getObjectMapper().convertValue(getByIdResponseHelper.getData(), BillerConsumerFeeResponse.class);
        assertNotNull(getByIdBillerConsumerFeeResponse);
        assertEquals(billerConsumerFeeRequest.getId(), getByIdBillerConsumerFeeResponse.getId());

        //getById -> Id is null
        assertThrows(ThirdPartyIntegrationException.class, () -> billerConsumerFeeService.getById(null));

        //toggle - from True to False
        ResponseHelper toggleResponseHelper = billerConsumerFeeService.toggle(billerConsumerFeeRequest.getId());
        assertNotNull(toggleResponseHelper.getData());
        BillerConsumerFeeResponse toggleBillerConsumerFeeResponse = CommonUtils.getObjectMapper().convertValue(toggleResponseHelper.getData(), BillerConsumerFeeResponse.class);
        assertFalse(toggleBillerConsumerFeeResponse.getActive());

        //toggle - from False to True
        toggleResponseHelper = billerConsumerFeeService.toggle(billerConsumerFeeRequest.getId());
        assertNotNull(toggleResponseHelper.getData());
        toggleBillerConsumerFeeResponse = CommonUtils.getObjectMapper().convertValue(toggleResponseHelper.getData(), BillerConsumerFeeResponse.class);
        assertTrue(toggleBillerConsumerFeeResponse.getActive());

        //toggle -> Id is null
        assertThrows(ThirdPartyIntegrationException.class, () -> billerConsumerFeeService.toggle(null));

        BillerConsumerFee billerConsumerFee = billerConsumerFeeRepo.findByBiller(BILLER_NAME);
        if (!Objects.isNull(billerConsumerFee)){
            billerConsumerFeeRepo.delete(billerConsumerFee);
        }

        billerConsumerFee = billerConsumerFeeRepo.findByBiller(BILLER_NAME_2);
        if (!Objects.isNull(billerConsumerFee)){
            billerConsumerFeeRepo.delete(billerConsumerFee);
        }
    }

    @Test
    void testGetFeeBearer() throws ThirdPartyIntegrationException {

        //save FeeBiller
        BillerConsumerFee billerConsumerFee = new BillerConsumerFee();
        billerConsumerFee.setValue(BigDecimal.ONE);
        billerConsumerFee.setThirdPartyName(ThirdPartyNames.BAXI);
        billerConsumerFee.setBiller("testBiller");
        billerConsumerFee.setFeeBearer(FeeBearer.CONSUMER);
        billerConsumerFee.setFeeType(FeeType.FIXED);
        billerConsumerFee = billerConsumerFeeRepo.save(billerConsumerFee);

        assertEquals(FeeBearer.CONSUMER, billerConsumerFeeService.getFeeBearer(ThirdPartyNames.BAXI, billerConsumerFee.getBiller()));

        billerConsumerFeeRepo.delete(billerConsumerFee);

    }

}