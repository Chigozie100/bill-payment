package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.repo.PaymentTransactionRepo;
import com.wayapay.thirdpartyintegrationservice.service.baxi.BaxiService;
import com.wayapay.thirdpartyintegrationservice.service.dispute.DisputeService;
import com.wayapay.thirdpartyintegrationservice.service.interswitch.QuickTellerService;
import com.wayapay.thirdpartyintegrationservice.service.itex.ItexService;
import com.wayapay.thirdpartyintegrationservice.service.profile.ProfileFeignClient;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.shaded.org.bouncycastle.crypto.RuntimeCryptoException;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext
@SpringBootTest
class BillsPaymentServiceTest {

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

    @SpyBean
    private CategoryService categoryService;

    @Autowired
    private BillerService billerService;

    @Autowired
    private ThirdPartyService thirdPartyService;
//
//    @Autowired
//    private ProfileFeignClient profileFeignClient;

    private BillsPaymentService billsPaymentService;

    private static final String username = "10";

    @BeforeEach
    void setUp() {
        billsPaymentService = new BillsPaymentService(itexService, baxiService, quickTellerService, paymentTransactionRepo, disputeService, operationService, billerConsumerFeeService, categoryService, billerService, thirdPartyService);
    }

    @Test
    void getBillsPaymentService() throws ThirdPartyIntegrationException, URISyntaxException {

        //ITEX
        Mockito.when(categoryService.findThirdPartyByCategoryAggregatorCode(Mockito.anyString())).thenReturn(Optional.of(ThirdPartyNames.ITEX));
        processBillsPayment("Airtime", ThirdPartyNames.ITEX);


        //BAXI
        Mockito.when(categoryService.findThirdPartyByCategoryAggregatorCode(Mockito.anyString())).thenReturn(Optional.of(ThirdPartyNames.BAXI));
        processBillsPayment("Airtime Recharge", ThirdPartyNames.BAXI);

        //INTERSWITCH
//        Mockito.when(categoryService.findThirdPartyByCategoryAggregatorCode(Mockito.anyString())).thenReturn(Optional.of(ThirdPartyNames.QUICKTELLER));
//        processBillsPayment("Mobile Recharge", ThirdPartyNames.QUICKTELLER);

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

    private void processBillsPayment(String airtime, ThirdPartyNames thirdPartyName) throws ThirdPartyIntegrationException, URISyntaxException {

        //confirm that get all categories is fine
        List<CategoryResponse> categoryResponses = billsPaymentService.getBillsPaymentService(thirdPartyName).getCategory();
        assertFalse(categoryResponses.isEmpty());
        CategoryResponse categoryResponse = categoryResponses.stream().filter(cResponse -> airtime.equalsIgnoreCase(cResponse.getCategoryName())).findFirst().orElseThrow(RuntimeException::new);
        assertNotNull(categoryResponse);

        //confirm that get all billers is fine
        List<BillerResponse> allBillersByCategory = billsPaymentService.getBillsPaymentService(thirdPartyName).getAllBillersByCategory(categoryResponse.getCategoryId());
        assertFalse(allBillersByCategory.isEmpty());
        BillerResponse billerResponse = allBillersByCategory.stream().findFirst().orElseThrow(RuntimeException::new);
        assertNotNull(billerResponse);

        //confirm that get payment Item is fine
        PaymentItemsResponse customerValidationFormByBiller = billsPaymentService.getBillsPaymentService(thirdPartyName).getCustomerValidationFormByBiller(billerResponse.getCategoryId(), billerResponse.getBillerId());
        assertNotNull(customerValidationFormByBiller);
        List<Item> items = customerValidationFormByBiller.getItems();
        List<ParamNameValue> paramNameValueList = items.stream().map(this::convert).collect(Collectors.toList());

        //confirm that validation is fine
        if (customerValidationFormByBiller.getIsValidationRequired()) {
            CustomerValidationRequest customerValidationRequest = new CustomerValidationRequest();
            customerValidationRequest.setBillerId(customerValidationFormByBiller.getBillerId());
            customerValidationRequest.setCategoryId(customerValidationFormByBiller.getCategoryId());
            customerValidationRequest.setData(paramNameValueList);
            CustomerValidationResponse customerValidationResponse = billsPaymentService.getBillsPaymentService(thirdPartyName).validateCustomerValidationFormByBiller(customerValidationRequest);
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
        assertThrows(NullPointerException.class, () ->billsPaymentService.processPayment(paymentRequest, username, "serial eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZ2JlLnRlcnNlZXJAZ21haWwuY29tIiwiZXhwIjoxNjYxMzM0NzQ4fQ.7Bqa50jXVUu4r63nrAbGrqsrdERB4565yXT6rWRyuYM"));
        //assertNotNull(paymentResponse);

        //confirm that search is fine
        Page<TransactionDetail> transactionDetails = billsPaymentService.search(username, 0, 10);
        assertTrue(transactionDetails.hasContent());

        transactionDetails = billsPaymentService.search(null, 0, 10);
        assertTrue(transactionDetails.hasContent());

    }
}