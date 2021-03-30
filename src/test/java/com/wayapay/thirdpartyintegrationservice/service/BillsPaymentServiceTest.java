package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.repo.PaymentTransactionRepo;
import com.wayapay.thirdpartyintegrationservice.service.baxi.BaxiService;
import com.wayapay.thirdpartyintegrationservice.service.dispute.DisputeService;
import com.wayapay.thirdpartyintegrationservice.service.interswitch.QuickTellerService;
import com.wayapay.thirdpartyintegrationservice.service.itex.ItexService;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.testcontainers.shaded.org.bouncycastle.crypto.RuntimeCryptoException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BillsPaymentServiceTest {

    @Autowired
    private ConfigService configService;

    @Autowired
    private ItexService itexService;

    @Autowired
    private BaxiService baxiService;

    @Autowired
    private QuickTellerService quickTellerService;

    @Autowired
    private PaymentTransactionRepo paymentTransactionRepo;

    @Autowired
    private DisputeService disputeService;

    @Autowired
    private OperationService operationService;

    @Autowired
    private BillerConsumerFeeService billerConsumerFeeService;

    private BillsPaymentService billsPaymentService;

    private static final String username = "testUserName";

    @BeforeEach
    void setUp() {
        billsPaymentService = new BillsPaymentService(configService, itexService, baxiService, quickTellerService, paymentTransactionRepo, disputeService, operationService, billerConsumerFeeService);
    }

    @Test
    void getBillsPaymentService() throws ThirdPartyIntegrationException {

        ThirdPartyNames activeThirdParty = configService.getActiveThirdParty();

        //ITEX
//        assertDoesNotThrow(() -> configService.setActiveThirdParty(ThirdPartyNames.ITEX));
//        processBillsPayment("Airtime");

        //BAXI
        assertDoesNotThrow(() -> configService.setActiveThirdParty(ThirdPartyNames.BAXI));
        processBillsPayment("Airtime Recharge");

        //INTERSWITCH
//        assertDoesNotThrow(() -> configService.setActiveThirdParty(ThirdPartyNames.QUICKTELLER));
//        processBillsPayment("Mobile Recharge");

        assertDoesNotThrow(() -> configService.setActiveThirdParty(ThirdPartyNames.BAXI));
    }

    private ParamNameValue convert(Item item){

        if (item.getSubItems().isEmpty()){
            return (Objects.isNull(item.getParamName()) || "null".equalsIgnoreCase(item.getParamName())) ? new ParamNameValue() : new ParamNameValue(item.getParamName(), "08011111111");
        }else {
            SubItem subItem = item.getSubItems().stream().findFirst().orElseThrow(RuntimeCryptoException::new);
            return new ParamNameValue(item.getParamName(), subItem.getId());
        }
    }

    private BigDecimal getAmount(List<Item> items){
        BigDecimal bigDecimal = new BigDecimal(items.stream().filter(item -> !item.getSubItems().isEmpty() && !Objects.isNull(item.getSubItems().stream().findFirst().orElseThrow(RuntimeCryptoException::new).getMinAmount())).findFirst().orElseThrow(RuntimeException::new).getSubItems().stream().findFirst().orElseThrow(RuntimeException::new).getMinAmount());
        if (bigDecimal.compareTo(BigDecimal.ZERO) == 0){
            return BigDecimal.valueOf(50L);
        }
        return bigDecimal;
    }

    private void processBillsPayment(String airtime) throws ThirdPartyIntegrationException {

        //confirm that get all categories is fine
        List<CategoryResponse> categoryResponses = billsPaymentService.getBillsPaymentService().getCategory();
        assertFalse(categoryResponses.isEmpty());
        CategoryResponse categoryResponse = categoryResponses.stream().filter(cResponse -> airtime.equalsIgnoreCase(cResponse.getCategoryName())).findFirst().orElseThrow(RuntimeException::new);
        assertNotNull(categoryResponse);

        //confirm that get all billers is fine
        List<BillerResponse> allBillersByCategory = billsPaymentService.getBillsPaymentService().getAllBillersByCategory(categoryResponse.getCategoryId());
        assertFalse(allBillersByCategory.isEmpty());
        BillerResponse billerResponse = allBillersByCategory.stream().findFirst().orElseThrow(RuntimeException::new);
        assertNotNull(billerResponse);

        //confirm that get payment Item is fine
        PaymentItemsResponse customerValidationFormByBiller = billsPaymentService.getBillsPaymentService().getCustomerValidationFormByBiller(billerResponse.getCategoryId(), billerResponse.getBillerId());
        assertNotNull(customerValidationFormByBiller);
        List<Item> items = customerValidationFormByBiller.getItems();
        List<ParamNameValue> paramNameValueList = items.stream().map(this::convert).collect(Collectors.toList());

        //confirm that validation is fine
        if (customerValidationFormByBiller.getIsValidationRequired()) {
            CustomerValidationRequest customerValidationRequest = new CustomerValidationRequest();
            customerValidationRequest.setBillerId(customerValidationFormByBiller.getBillerId());
            customerValidationRequest.setCategoryId(customerValidationFormByBiller.getCategoryId());
            customerValidationRequest.setData(paramNameValueList);
            CustomerValidationResponse customerValidationResponse = billsPaymentService.getBillsPaymentService().validateCustomerValidationFormByBiller(customerValidationRequest);
            assertNotNull(customerValidationResponse);
            items.addAll(customerValidationResponse.getItems());
            paramNameValueList.addAll(items.stream().map(this::convert).collect(Collectors.toList()));
        }

        //confirm that payment is fine
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setSourceWalletAccountNumber("OV6G67ODA4");
        paymentRequest.setData(paramNameValueList);
        paymentRequest.setCategoryId(customerValidationFormByBiller.getCategoryId());
        paymentRequest.setBillerId(customerValidationFormByBiller.getBillerId());
        paymentRequest.setAmount(getAmount(items));
        PaymentResponse paymentResponse = billsPaymentService.processPayment(paymentRequest, username);
        assertNotNull(paymentResponse);

        //confirm that search is fine
        Page<TransactionDetail> transactionDetails = billsPaymentService.search(username, 0, 10);
        assertTrue(transactionDetails.hasContent());

        transactionDetails = billsPaymentService.search(null, 0, 10);
        assertTrue(transactionDetails.hasContent());

    }
}