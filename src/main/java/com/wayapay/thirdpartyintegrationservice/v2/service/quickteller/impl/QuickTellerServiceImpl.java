package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.impl;

import com.wayapay.thirdpartyintegrationservice.v2.dto.BillCategoryName;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.AuthResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.CustomerTokenValidationDto;
import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProvider;
import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProviderBiller;
import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProviderCategory;
import com.wayapay.thirdpartyintegrationservice.v2.proxyclient.AuthProxy;
import com.wayapay.thirdpartyintegrationservice.v2.repository.*;
import com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.QuickTellerProxy;
import com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.QuickTellerService;
import com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.RequestHeaders;
import com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto.*;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.wayapay.thirdpartyintegrationservice.v2.dto.Constants.QUCIK_TELLER_INVALID_TOKEN;
import static com.wayapay.thirdpartyintegrationservice.v2.dto.Constants.QUICK_TELLER_SUCCESSFUL;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuickTellerServiceImpl implements QuickTellerService {

    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceProviderBillerRepository serviceProviderBillerRepository;
    private final ServiceProviderCategoryRepository serviceProviderCategoryRepository;
    private final ServiceProviderProductRepository serviceProviderProductRepository;
    private final ServiceProviderProductBundleRepository serviceProviderProductBundleRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final AuthProxy authProxy;
    private final QuickTellerProxy quickTellerProxy;
    @Autowired
    private RequestHeaders requestHeaders;


    @Value("${app.config.quickteller.base-url}")
    private String baseUrl;

    @Value("${app.config.quickteller.biller-category-url}")
    private String billerCategoryUrl;

    @Value("${app.config.quickteller.billers-url}")
    private String billerUrl;

    @Value("${app.config.quickteller.biller-payment-item-url}")
    private String billerPaymentItemUrl;

    @Value("${app.config.quickteller.customer-validation-url}")
    private String customerValidationUrl;

    @Value("${app.config.quickteller.send-payment-advice-url}")
    private String sendPaymentAdviceUrl;

    @Value("${app.config.quickteller.terminal-id}")
    private String providerTerminalId;

    @Value("${app.config.quickteller.transaction-ref-code}")
    private String providerReferenceCode;

    @Value("${app.config.quickteller.client-id}")
    private String clientId;

    @Value("${app.config.quickteller.secret}")
    private String clientSecret;


    @Override
    public ApiResponse<?> fetchHeaders(String token, String url) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Map<String, String> headers = requestHeaders.getISWAuthSecurityHeaders(clientId,clientSecret,url, HttpMethod.GET.toString());
            HashMap<String, String> map = new HashMap<>();
            map.put("Auth",getAuthorisation(headers));
            map.put("Nonce",getNonce(headers));
            map.put("SignatureMethod",getSignatureMethod(headers));
            map.put("Signature",getSignature(headers));
            map.put("Timestamp",getTimeStamp(headers));

            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Success",map);
        }catch (Exception ex){
            log.error("::Error Fetch {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process your request, try again later",null);
        }
    }

    @Override
    public ApiResponse<?> fetchCategories(String token) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            String bUrl = baseUrl + billerCategoryUrl;
            Map<String, String> headers = requestHeaders.getISWAuthSecurityHeaders(clientId,clientSecret,bUrl, HttpMethod.GET.toString());
            CategoryResponse categoryResponse = quickTellerProxy.getCategory(getAuthorisation(headers),getSignature(headers),getNonce(headers),getTimeStamp(headers),getSignatureMethod(headers));
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Success",categoryResponse);
        }catch (Exception ex){
            log.error("::Error Fetch categoryResponse {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process your request, try again later",null);
        }
    }

    @Override
    public ApiResponse<?> fetchBiller(String token, String categoryId) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);


            String bUrl = baseUrl + "/api/v2/quickteller/categorys/"+categoryId+"/billers";
            Map<String, String> headers = requestHeaders.getISWAuthSecurityHeaders(clientId,clientSecret,bUrl, HttpMethod.GET.toString());
            GetAllBillersResponse billersResponse = quickTellerProxy.getBillerByCategoryId(categoryId,getAuthorisation(headers),getSignature(headers),getNonce(headers),getTimeStamp(headers),getSignatureMethod(headers));
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Success",billersResponse);
        }catch (Exception ex){
            log.error("::Error Fetch {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process your request, try again later",null);
        }
    }

    @Override
    public ApiResponse<?> createQuickTellerServiceProviderCategory(String token, Long serviceProviderId) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Provider not found",null);

            CategoryResponse categoryResponse;
            try {
                String categoryUrl = baseUrl + billerCategoryUrl;

                Map<String, String> headers = requestHeaders.getISWAuthSecurityHeaders(clientId,clientSecret,categoryUrl, HttpMethod.GET.toString());
                categoryResponse = quickTellerProxy.getCategory(getAuthorisation(headers),getSignature(headers),getNonce(headers),getTimeStamp(headers),getSignatureMethod(headers));
                log.info("::CategoryResponse {}",categoryResponse);
                if(categoryResponse.getCategorys() == null )
                    return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to fetch QuickTeller categories, try again later",null);
            }catch (FeignException ex){
                log.error("::Error CategoryResponse {}",ex.getLocalizedMessage());
                String msg = ex.contentUTF8();
                int status = ex.status();
                if(msg != null && msg !="")
                    return new ApiResponse<>(false,status,msg,null);

                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Fail to fetch QuickTeller categories, try gain later",null);
            }

            List<ServiceProviderCategory> categoryList = new ArrayList<>();
            for(CategoryDetail category: categoryResponse.getCategorys()){

                if(category.getCategoryname().equalsIgnoreCase("Insurance") ||
                        category.getCategoryname().equalsIgnoreCase("Receive Money") ||
                        category.getCategoryname().equalsIgnoreCase("Transfer Money")||
                        category.getCategoryname().equalsIgnoreCase("Quickteller Business")||
                        category.getCategoryname().equalsIgnoreCase("PayChoice")||
                        category.getCategoryname().equalsIgnoreCase("Mobile Money")||
                        category.getCategoryname().equalsIgnoreCase("Microfinance")||
                        category.getCategoryname().equalsIgnoreCase("Funds Transfer")||
                        category.getCategoryname().equalsIgnoreCase("Products and Services")){
                    continue;
                }

                String serviceType = formatAndMatchCategory(category.getCategoryname().toLowerCase());
                if(serviceType == null)
                    continue;

                String name = formatName(category.getCategoryname());
                Optional<ServiceProviderCategory> exist = serviceProviderCategoryRepository.findByServiceProviderAndTypeAndIsActiveAndIsDeleted(serviceProvider.get(),serviceType,true,false);
                if(exist.isPresent())
                    continue;

                Optional<ServiceProviderCategory> existByName = serviceProviderCategoryRepository.findByNameAndServiceProviderAndIsActiveAndIsDeleted(name,serviceProvider.get(),true,false);
                if(existByName.isPresent())
                    continue;

                ServiceProviderCategory providerCategory = new ServiceProviderCategory();
                providerCategory.setServiceProvider(serviceProvider.get());
                if(category.getCategoryname().toLowerCase().contains("utility")){
                    providerCategory.setName("Power / Electricity / Water / Utility");
                }else {
                    providerCategory.setName(category.getCategoryname());
                }
                providerCategory.setType(serviceType);
                providerCategory.setDescription(category.getCategorydescription());
                providerCategory.setServiceCategoryId(category.getCategoryid());
                providerCategory.setCreatedBy(response.getData().getEmail());
                providerCategory.setModifiedBy(response.getData().getEmail());
                categoryList.add(providerCategory);
            }
            serviceProviderCategoryRepository.saveAll(categoryList);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Category created successful",categoryList);
        }catch (Exception ex){
            log.error("::Error createQuickTellerServiceProviderCategory {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process your request, try again later",null);
        }
    }


    @Override
    public ApiResponse<?> createQuickTellerServiceProviderBiller(String token, Long serviceProviderId, Long serviceProviderCategoryId) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Provider not found",null);

            Optional<ServiceProviderCategory> serviceProviderCategory = serviceProviderCategoryRepository.findByIdAndServiceProviderAndIsActiveAndIsDeleted(serviceProviderCategoryId,serviceProvider.get(),true,false);
            if(!serviceProviderCategory.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider category not found",null);

            GetAllBillersResponse billersResponse;
            try {
                String categoryId = serviceProviderCategory.get().getServiceCategoryId();
                String billerCategoryUrl = baseUrl + "/api/v2/quickteller/categorys/"+categoryId+"/billers";

                Map<String, String> headers = requestHeaders.getISWAuthSecurityHeaders(clientId,clientSecret,billerCategoryUrl, HttpMethod.GET.toString());
                billersResponse = quickTellerProxy.getBillerByCategoryId(categoryId,getAuthorisation(headers),getSignature(headers),getNonce(headers),getTimeStamp(headers),getSignatureMethod(headers));
                log.info("::BillerCategoryResponse {}",billersResponse);
                if(billersResponse.getBillers() == null )
                    return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to fetch QuickTeller billers, try again later",null);

            }catch (FeignException ex){
                log.error("::Error BillerCategoryResponse {}",ex.getLocalizedMessage());
                String msg = ex.contentUTF8();
                int status = ex.status();
                if(msg != null && msg !="")
                    return new ApiResponse<>(false,status,msg,null);
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Fail to fetch QuickTeller billers, try gain later",null);
            }

            List<ServiceProviderBiller> billerList = new ArrayList<>();
            for(BillerDetail biller: billersResponse.getBillers()){
                try {
                    Optional<ServiceProviderBiller> betting = serviceProviderBillerRepository
                            .findByNameAndServiceProviderCategoryAndIsActiveAndIsDeleted(biller.getBillername(),serviceProviderCategory.get(),true,false);
                    if(betting.isPresent())
                        continue;

                    ServiceProviderBiller eBiller = new ServiceProviderBiller();
                    eBiller.setServiceProviderCategory(serviceProviderCategory.get());
                    eBiller.setCreatedBy(response.getData().getEmail());
                    eBiller.setModifiedBy(response.getData().getEmail());
                    eBiller.setProductId(biller.getProductCode());
                    eBiller.setBillerId(biller.getBillerid());
                    eBiller.setName(biller.getBillername());
                    eBiller.setType(biller.getBillerid());
                    eBiller.setDescription(biller.getNarration());
                    eBiller.setImageLogo(biller.getLogoUrl());
                    eBiller.setHasProduct(Boolean.FALSE);
                    if(serviceProviderCategory.get().getType().equals(BillCategoryName.electricity.name())){
                        if(biller.getBillername().toLowerCase().contains("prepaid")){
                            eBiller.setPrepaidName(biller.getBillername());
                            eBiller.setIsPrepaid(Boolean.TRUE);
                        }else if(biller.getBillername().toLowerCase().contains("postpaid")){
                            eBiller.setIsPostpaid(Boolean.TRUE);
                            eBiller.setPostpaidName(biller.getBillername());
                            eBiller.setEPostpaidBillerId(biller.getBillerid());
                        }
                        eBiller.setIsRequiredIdVerification(Boolean.TRUE);
                    }else if(serviceProviderCategory.get().getType().equals(BillCategoryName.cabletv.name())||
                            serviceProviderCategory.get().getType().equals(BillCategoryName.tax.name())||
                            serviceProviderCategory.get().getType().equals(BillCategoryName.subscription.name())||
                            serviceProviderCategory.get().getType().equals(BillCategoryName.betting.name())||
                            serviceProviderCategory.get().getType().equals(BillCategoryName.insurance.name())){
                        eBiller.setIsRequiredIdVerification(Boolean.TRUE);
                    }else {
                        eBiller.setIsRequiredIdVerification(Boolean.FALSE);
                    }
                    eBiller.setServiceProviderId(serviceProviderCategory.get().getServiceProvider().getId());
                    billerList.add(eBiller);
                }catch (Exception ex){
                    log.error("::Error billerList {}",ex.getLocalizedMessage());
                    ex.printStackTrace();
                    continue;
                }
            }
            serviceProviderBillerRepository.saveAll(billerList);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Service provider biller created successful",billerList);
        }catch (Exception ex){
            log.error("::Error createQuickTellerServiceProviderBiller {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process your request, try again later",null);
        }
    }


    @Override
    public ApiResponse<?> verifyCustomerAccountNumberOrSmartCardOrMeterNumber(String type, String account, String categoryType) {
        try {
            String paymentItemUrl = baseUrl + billerPaymentItemUrl;

            GetBillerPaymentItemResponse paymentItemResponse;
            try {
                Map<String, String> headers = requestHeaders.getISWAuthSecurityHeaders(clientId,clientSecret,paymentItemUrl, HttpMethod.GET.toString());
                paymentItemResponse = quickTellerProxy.getBillerPaymentItems(type,getAuthorisation(headers),getSignature(headers),getNonce(headers),getTimeStamp(headers),getSignatureMethod(headers),providerTerminalId);
                log.info("::PaymentItemResponse {}",paymentItemResponse);
                if(paymentItemResponse.getPaymentitems() == null)
                    return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to generate payment invoice, try again later",null);
            }catch (FeignException ex){
                log.error("::Error Generate Payment Info {}",ex.getLocalizedMessage());
                String msg = ex.contentUTF8();
                int status = ex.status();
                if(msg != null && msg !="")
                    return new ApiResponse<>(false,status,msg,null);

                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Fail to generate payment invoice, try gain later",null);
            }

            String paymentCode= "";
            if(paymentItemResponse.getPaymentitems().size() > 0){
                paymentCode = paymentItemResponse.getPaymentitems().get(0).getPaymentCode();
            }
            String validateUrl = baseUrl + customerValidationUrl;
            QuickTellerCustomerValidationResponse validationResponse;
            try {
                QuickTellerCustomerValidationRequest validationRequest = new QuickTellerCustomerValidationRequest();
                ValidationRequest request = new ValidationRequest();
                request.setCustomerId(account);
                request.setPaymentCode(paymentCode);
                List<ValidationRequest> requestList = new ArrayList<>();
                requestList.add(request);
                validationRequest.setCustomers(requestList);
                log.info("::ValidateRequestPayload {}",validationRequest);
                Map<String, String> headers = requestHeaders.getISWAuthSecurityHeaders(clientId,clientSecret,validateUrl, HttpMethod.GET.toString());
                validationResponse = quickTellerProxy.validateCustomerInfo(validationRequest,getAuthorisation(headers),getSignature(headers),getNonce(headers),getTimeStamp(headers),getSignatureMethod(headers),providerTerminalId);
                log.info("::ValidationResponse {}",validationResponse);
                 if(validationResponse.getCustomers() == null)
                     return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Oops!\n Unable to validate your token id, try again later",null);
            }catch (FeignException ex){
                log.error("::Error validateCustomerId {}",ex.getLocalizedMessage());
                String msg = ex.contentUTF8();
                int status = ex.status();
                if(msg != null && msg !="")
                    return new ApiResponse<>(false,status,msg,null);

                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Fail to validate your token id, try gain later",null);
            }

            CustomerTokenValidationDto tokenValidationDto = new CustomerTokenValidationDto();
            String name;
            String accountNum;
            String respCode;
            String msg = "Invalid Token Id";
            if(validationResponse.getCustomers().size() > 0){
                name = validationResponse.getCustomers().get(0).getFullName();
                accountNum = validationResponse.getCustomers().get(0).getCustomerId();
                respCode  = validationResponse.getCustomers().get(0).getResponseCode();
                if(validationResponse.getCustomers().get(0).getResponseDescription() != null)
                    msg = validationResponse.getCustomers().get(0).getResponseDescription();
            }else {
                name = null;
                accountNum = null;
                respCode = QUCIK_TELLER_INVALID_TOKEN;
            }
            tokenValidationDto.setName(name);
            tokenValidationDto.setAccountNumber(accountNum);
            if(!respCode.equals(QUICK_TELLER_SUCCESSFUL))
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,msg,null);

            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Success",tokenValidationDto);
        }catch (Exception ex){
            log.error("::Error verifyCustomerAccountNumberOrSmartCardOrMeterNumber {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process your request, try again later",null);
        }
    }


    private String getAuthorisation(Map<String, String> headers){
        return headers.get("AUTHORIZATION");
    }

    private String getNonce(Map<String, String> headers){
        return headers.get("NONCE");
    }

    private String getTimeStamp(Map<String, String> headers){
        return headers.get("TIMESTAMP");
    }

    private String getSignature(Map<String, String> headers){
        return headers.get("SIGNATURE");
    }

    private String getSignatureMethod(Map<String, String> headers){
        return headers.get("SIGNATURE_METHOD");
    }

    private String formatAndMatchCategory(String name){
        try {

            if(name.contains(BillCategoryName.betting.name()) || name.contains("lottery"))
                return BillCategoryName.betting.name();

            if(name.contains("cable") && name.contains("tv"))
                return BillCategoryName.cabletv.name();

            if(name.contains("utility") && name.contains("utility bills"))
                return BillCategoryName.electricity.name();

            if(name.contains("mobile recharge") && name.contains("recharge"))
                return BillCategoryName.airtime.name();

            if(name.contains("phone bills"))
                return BillCategoryName.databundle.name();

            if(name.contains("subscriptions"))
                return BillCategoryName.subscription.name();

            if(name.contains("embassy"))
                return BillCategoryName.embassy.name();

            if(name.contains("tax"))
                return BillCategoryName.tax.name();

            if(name.contains("donations"))
                return BillCategoryName.donation.name();

            if(name.contains("insurance"))
                return BillCategoryName.insurance.name();

            if(name.contains("airlines"))
                return BillCategoryName.airline.name();

            if(name.contains("transport"))
                return BillCategoryName.transport.name();

            return name;
        }catch (Exception ex){
            log.error("::Error formatAndMatchCategory {}",ex.getLocalizedMessage());
            return null;
        }
    }

    private String formatName(String name){
        String billerName;
        if(name.toLowerCase().contains("utility")){
            billerName = "Power / Electricity / Water / Utility";
        }else {
            billerName = name;
        }
        return billerName;
    }

}
