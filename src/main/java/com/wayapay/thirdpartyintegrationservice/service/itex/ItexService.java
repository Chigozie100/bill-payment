package com.wayapay.thirdpartyintegrationservice.service.itex;

import com.wayapay.thirdpartyintegrationservice.config.AppConfig;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.IThirdPartyService;
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
    private static List<String> categoryElectricity = Arrays.asList("ikedc","aedc","phedc","eedc","ibedc","ekedc", "kedco");
    private static List<String> categoryAirtime = Arrays.asList("mtnvtu","9mobilevtu","glovtu","glovot","glovos","airtelpin", "airtelvtu");
    private static List<String> categoryData = Arrays.asList("mtndata","9mobiledata","glodata", "airteldata");
    private static List<String> categoryCableTv = Arrays.asList("multichoice", "startimes");
    private static List<String> categoryInternet = Collections.singletonList("smile");
    private static List<String> categoryRemita = Collections.singletonList(REMITA);
    private static List<String> categoryLcc = Collections.singletonList(LCC);

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
    public List<String> getCategory() {
        return Arrays.asList(ELECTRICITY, AIRTIME, DATA, CABLE_TV, INTERNET, REMITA, LCC);
    }

    @Override
    public List<String> getAllBillersByCategory(String category) throws ThirdPartyIntegrationException {

        switch (category){
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
                throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Unknown category name provided");
        }

    }

    @Override
    public List<String> getCustomerValidationFormByBiller(String billerName) throws ThirdPartyIntegrationException {

        if (categoryElectricity.contains(billerName)){

            //get the params, then also get it sublist


            return new ArrayList<>();
        } else if (categoryAirtime.contains(billerName)){
            return new ArrayList<>();
        } else if (categoryData.contains(billerName)){
            return new ArrayList<>();
        } else if (categoryInternet.contains(billerName)){
            return new ArrayList<>();
        } else if (categoryCableTv.contains(billerName)){
            return new ArrayList<>();
        } else if (categoryRemita.contains(billerName)){
            return new ArrayList<>();
        } else if (categoryLcc.contains(billerName)){
            return new ArrayList<>();
        } else {
           throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Unknown biller name provided");
        }
    }

    @Override
    public void validateCustomerValidationFormByBiller() {

    }

    @Override
    public void processPayment() {

        //ensure that the MONEY to be paid is secured alongside the FEE
        //Initiate Payment
        //#Async : Confirm Payment Status by making a Transaction status, if not successfull, then reverse the MONEY and the FEE.

    }
}

class BillerName{
    static final String ELECTRICITY = "Electricity";
    static final String AIRTIME = "Airtime";
    static final String DATA = "Data";
    static final String CABLE_TV = "CableTv";
    static final String INTERNET = "Internet";
    static final String REMITA = "Remita";
    static final String LCC = "LCC";
}