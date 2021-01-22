package com.wayapay.thirdpartyintegrationservice.service.itex;

import com.wayapay.thirdpartyintegrationservice.config.AppConfig;
import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.IThirdPartyService;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.wayapay.thirdpartyintegrationservice.service.itex.BillerName.*;

@Slf4j
@Service
public class ItexService implements IThirdPartyService {

    private ItexFeignClient feignClient;
    private AppConfig appConfig;
    private ItexUtil itexUtil;
    private List<SubItem> channelsSubList = new ArrayList<>();
    private List<SubItem> paymentMethodSubList = new ArrayList<>();
    private static final String AMOUNT = "amount";
    private static final String ACCOUNT = "account";
    private static List<BillerResponse> categoryElectricity = Arrays.asList(new BillerResponse("ikedc", "IKEDC", ELECTRICITY), new BillerResponse("aedc", "AEDC", ELECTRICITY), new BillerResponse("phedc", "PHEDC", ELECTRICITY), new BillerResponse("eedc", "EEDC", ELECTRICITY), new BillerResponse("ibedc", "IBEDC", ELECTRICITY), new BillerResponse("ekedc", "EKEDC", ELECTRICITY), new BillerResponse("kedco", "KEDCO", ELECTRICITY));
    private static List<BillerResponse> categoryAirtime = Arrays.asList(new BillerResponse("mtnvtu", "MTN VTU", AIRTIME), new BillerResponse("9mobilevtu", "9MOBILE VTU", AIRTIME), new BillerResponse("glovtu", "GLO VTU", AIRTIME), new BillerResponse("glovot", "GLO VOT", AIRTIME), new BillerResponse("glovos", "GLO VOS", AIRTIME), new BillerResponse("airtelpin", "AIRTEL PIN", AIRTIME), new BillerResponse("airtelvtu", "AIRTEL VTU", AIRTIME));
    private static List<BillerResponse> categoryData = Arrays.asList(new BillerResponse( "mtndata","MTN DATA", DATA), new BillerResponse( "9mobiledata","9MOBILE DATA", DATA), new BillerResponse( "glodata","GLO DATA", DATA), new BillerResponse( "airteldata","AIRTEL DATA", DATA));
    private static List<BillerResponse> categoryCableTv = Arrays.asList(new BillerResponse("multichoice", "multichoice", CABLE_TV), new BillerResponse("startimes", "STARTIMES",CABLE_TV));
    private static List<BillerResponse> categoryInternet = Collections.singletonList(new BillerResponse("smile", "SMILE", INTERNET));
    private static List<BillerResponse> categoryRemita = Collections.singletonList(new BillerResponse(REMITA, REMITA, REMITA));
    private static List<BillerResponse> categoryLcc = Collections.singletonList(new BillerResponse(LCC, LCC, LCC));

    public ItexService(ItexFeignClient feignClient, AppConfig appConfig, ItexUtil itexUtil) {
        this.feignClient = feignClient;
        this.appConfig = appConfig;
        this.itexUtil = itexUtil;
    }

    private Optional<String> getAuthApiToken(){
        ResponseEntity<Object> objectResponseEntity = feignClient.getAuthApiToken(new AuthApiTokenRequest(appConfig.getItex().getWalletId(), appConfig.getItex().getUsername(), appConfig.getItex().getPassword(), appConfig.getItex().getUniqueApiIdentifier()));
        return Optional.empty();
    }

    private Optional<String> generateEncryptedPin(){
        ResponseEntity<Object> objectResponseEntity = feignClient.generateEncryptedPin(new EncryptedPinRequest(appConfig.getItex().getWalletId(), appConfig.getItex().getUsername(), appConfig.getItex().getPassword(), appConfig.getItex().getPayVicePin()));
        return Optional.empty();
    }

    private Optional<String> generateSignature(String jsonRequestAsString){
        try {
            return Optional.of(itexUtil.generateHmac256(jsonRequestAsString, appConfig.getItex().getHmacsha256key()));
        } catch (InvalidKeyException e) {
            log.error("InvalidKeyException: ", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException: ", e);
        }

        return Optional.empty();
    }

    @Override
    public List<CategoryResponse> getCategory() {
        return Arrays.asList(new CategoryResponse(ELECTRICITY, ELECTRICITY), new CategoryResponse(AIRTIME, AIRTIME), new CategoryResponse(DATA, DATA), new CategoryResponse(CABLE_TV, CABLE_TV), new CategoryResponse(INTERNET, INTERNET), new CategoryResponse(REMITA, REMITA), new CategoryResponse(LCC, LCC));
    }

    //todo cache the request and response
    @Override
    public List<BillerResponse> getAllBillersByCategory(String categoryId) throws ThirdPartyIntegrationException {

        switch (categoryId){
            case ELECTRICITY:
                return categoryElectricity;

            case AIRTIME:
                return categoryAirtime;

            case DATA:
                return categoryData;

            case CABLE_TV:
                return categoryCableTv;

            case INTERNET:
                return categoryInternet;

            case REMITA:
                return categoryRemita;

            case LCC:
                return categoryLcc;

            default:
                throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Unknown category Id provided");
        }

    }

    @Override
    public PaymentItemsResponse getCustomerValidationFormByBiller(String categoryId, String billerId) throws ThirdPartyIntegrationException {

        if (CommonUtils.isEmpty(categoryId) || CommonUtils.isEmpty(billerId)){
            log.error("categoryId => {} or billerId => {} is empty/null ", categoryId, billerId);
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid category or biller provided");
        }

        if (categoryElectricity.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(billerId))){
            return getElectricityPaymentItems(billerId, categoryId);
        } else if (categoryAirtime.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(billerId))){
            return getAirtimePaymentItems(billerId, categoryId);
        } else if (categoryData.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(billerId))){
            return getDataPaymentItems(billerId, categoryId);
        } else if (categoryInternet.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(billerId))){
            return getInternetPaymentItems(billerId, categoryId);
        } else if (categoryCableTv.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(billerId))){
            return getCableTvPaymentItems(billerId, categoryId);
        } else if (categoryRemita.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(billerId))){
            return getRemitaPaymentItems(billerId, categoryId);
        } else if (categoryLcc.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(billerId))){
            return getLCCPaymentItems(billerId, categoryId);
        } else {
           throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Unknown biller name provided");
        }
    }

    @Override
    public void validateCustomerValidationFormByBiller() {

        //for cableTv and Internet, the validationResponse should contain the bouquets or bundles

    }

    @Override
    public void processPayment() {

        //ensure that the MONEY to be paid is secured alongside the FEE
        //Initiate Payment
        //#Async : Confirm Payment Status by making a Transaction status, if not successfull, then reverse the MONEY and the FEE.

    }

    private Item getChannelsAsItem(){
        if (channelsSubList.isEmpty()) {
            channelsSubList.add(new SubItem("ATM"));
            channelsSubList.add(new SubItem("B2B"));
            channelsSubList.add(new SubItem("WEB"));
            channelsSubList.add(new SubItem("MOBILE"));
            channelsSubList.add(new SubItem("ANDROIDPOS"));
            channelsSubList.add(new SubItem("LINUXPOS"));
        }
        Item itemChannel = new Item();
        itemChannel.setParamName("channel");
        itemChannel.getSubItems().addAll(channelsSubList);
        return itemChannel;
    }

    private Item getPaymentMethodAsItem(){
        if (paymentMethodSubList.isEmpty()) {
            paymentMethodSubList.add(new SubItem("card"));
            paymentMethodSubList.add(new SubItem("cash"));
        }
        Item itemPaymentMethod = new Item();
        itemPaymentMethod.setParamName("paymentMethod");
        itemPaymentMethod.getSubItems().addAll(paymentMethodSubList);
        return itemPaymentMethod;
    }

    private PaymentItemsResponse getElectricityPaymentItems(String billerId, String categoryId){
        //get the params, then also get it sublist
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        paymentItemsResponse.getItems().add(new Item("meterNo"));
        paymentItemsResponse.getItems().add(new Item(AMOUNT));
        Item itemAccountType = new Item();
        itemAccountType.setParamName("accountType");
        itemAccountType.getSubItems().add(new SubItem("prepaid"));
        itemAccountType.getSubItems().add(new SubItem("postpaid"));
        itemAccountType.getSubItems().add(new SubItem("smartcard"));
        paymentItemsResponse.getItems().add(itemAccountType);
        paymentItemsResponse.getItems().add(getChannelsAsItem());
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getAirtimePaymentItems(String billerId, String categoryId){
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(false);
        paymentItemsResponse.getItems().add(new Item("phone"));
        paymentItemsResponse.getItems().add(new Item(AMOUNT));
        paymentItemsResponse.getItems().add(getPaymentMethodAsItem());
        paymentItemsResponse.getItems().add(getChannelsAsItem());
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getDataPaymentItems(String billerId, String categoryId){
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        paymentItemsResponse.getItems().add(getChannelsAsItem());
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getCableTvPaymentItems(String billerId, String categoryId){
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        paymentItemsResponse.getItems().add(new Item(ACCOUNT));
        paymentItemsResponse.getItems().add(new Item(AMOUNT));
        Item itemType = new Item("type");
        itemType.setIsAccountFixed(true);
        itemType.getSubItems().add(new SubItem("DSTV"));
        itemType.getSubItems().add(new SubItem("GOTV"));
        itemType.getSubItems().add(new SubItem("default", "DEFAULT"));
        SubItem subItemTopUp = new SubItem("topup", "TOPUP");
        subItemTopUp.setMinAmount("20");
        itemType.getSubItems().add(subItemTopUp);
        paymentItemsResponse.getItems().add(itemType);
        paymentItemsResponse.getItems().add(getChannelsAsItem());
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getInternetPaymentItems(String billerId, String categoryId){
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        paymentItemsResponse.getItems().add(new Item(ACCOUNT));
        Item itemType = new Item("type");
        itemType.getSubItems().add(new SubItem("phone"));
        itemType.getSubItems().add(new SubItem("email"));
        itemType.getSubItems().add(new SubItem(ACCOUNT));
        paymentItemsResponse.getItems().add(getChannelsAsItem());
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getRemitaPaymentItems(String billerId, String categoryId){
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        paymentItemsResponse.getItems().add(new Item("rrr"));
        paymentItemsResponse.getItems().add(getChannelsAsItem());
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getLCCPaymentItems(String billerId, String categoryId){
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        paymentItemsResponse.getItems().add(new Item(ACCOUNT));
        paymentItemsResponse.getItems().add(getChannelsAsItem());
        return paymentItemsResponse;
    }
}

class BillerName {

    static final String ELECTRICITY = "Electricity";
    static final String AIRTIME = "Airtime";
    static final String DATA = "Data";
    static final String CABLE_TV = "CableTv";
    static final String INTERNET = "Internet";
    static final String REMITA = "Remita";
    static final String LCC = "LCC";

    private BillerName(){

    }
}