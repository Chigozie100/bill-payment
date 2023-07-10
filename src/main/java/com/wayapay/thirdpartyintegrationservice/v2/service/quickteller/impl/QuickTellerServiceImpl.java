package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapay.thirdpartyintegrationservice.v2.dto.BillCategoryName;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.AuthResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.CustomerTokenValidationDto;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.GeneralPaymentResponseDto;
import com.wayapay.thirdpartyintegrationservice.v2.entity.*;
import com.wayapay.thirdpartyintegrationservice.v2.proxyclient.AuthProxy;
import com.wayapay.thirdpartyintegrationservice.v2.repository.*;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto.Plan;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto.Pricing;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
    @Autowired
    private ObjectMapper objectMapper;


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

    @Value("${app.config.quickteller.query-transaction}")
    private String queryTransactionUrl;

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
    public ApiResponse<?> getPayItem(String token, String billerId) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            String bUrl = baseUrl + "/api/v2/quickteller/billers/"+billerId+"/paymentitems";
            Map<String, String> headers = requestHeaders.getISWAuthSecurityHeaders(clientId,clientSecret,bUrl, HttpMethod.GET.toString());
            GetBillerPaymentItemResponse itemResponse = quickTellerProxy.getBillerPaymentItems(billerId,getAuthorisation(headers),getSignature(headers),getNonce(headers),getTimeStamp(headers),getSignatureMethod(headers),providerTerminalId);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Success",itemResponse);
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

                if(unProcessQuickTellerTestCategory().contains(category.getCategoryname().toLowerCase()) ||
                        unProcessQuickTellerProductionCategory().contains(category.getCategoryname().toLowerCase())){
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
                if(category.getCategoryname().toLowerCase().contains("utility") ||
                        category.getCategoryname().toLowerCase().contains("utilities")){
                    providerCategory.setName("Power / Electricity / Water / Utility");
                }else {
                    providerCategory.setName(name);
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
                    eBiller.setShortName(biller.getShortName());
                    eBiller.setHasProduct(Boolean.TRUE);
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
                    }else if(serviceProviderCategory.get().getType().equals(BillCategoryName.airtime.name())||
                            serviceProviderCategory.get().getType().equals(BillCategoryName.databundle.name())||
                            serviceProviderCategory.get().getType().equals(BillCategoryName.epin.name())||
                            serviceProviderCategory.get().getType().equals(BillCategoryName.international_airtime.name())){
                        eBiller.setIsRequiredIdVerification(Boolean.FALSE);
                    }else {
                        eBiller.setIsRequiredIdVerification(Boolean.TRUE);
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
    public ApiResponse<?> createQuickTellerServiceProviderProductByBiller(String token, Long serviceProviderId, Long serviceProviderCategoryId) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"QuickTeller provider not found",null);

            Optional<ServiceProviderCategory> serviceProviderCategory = serviceProviderCategoryRepository.findByIdAndServiceProviderAndIsActiveAndIsDeleted(serviceProviderCategoryId,serviceProvider.get(),true,false);
            if(!serviceProviderCategory.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"QuickTeller service category not found, please initiate service category before calling this service",null);

            List<ServiceProviderBiller> serviceProviderBillerList = serviceProviderBillerRepository.findAllByServiceProviderIdAndServiceProviderCategoryAndIsActiveAndIsDeleted(serviceProviderId,serviceProviderCategory.get(),true,false);
            if(serviceProviderBillerList.size() < 1)
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"QuickTeller biller not found, please initiate service category before calling this service",null);

            for (ServiceProviderBiller biller: serviceProviderBillerList){
                GetBillerPaymentItemResponse itemResponse;
                try {
                    String bUrl = baseUrl + "/api/v2/quickteller/billers/"+biller.getBillerId()+"/paymentitems";
                    Map<String, String> headers = requestHeaders.getISWAuthSecurityHeaders(clientId,clientSecret,bUrl, HttpMethod.GET.toString());
                    itemResponse = quickTellerProxy.getBillerPaymentItems(biller.getBillerId(),getAuthorisation(headers),getSignature(headers),getNonce(headers),getTimeStamp(headers),getSignatureMethod(headers),providerTerminalId);
                    if(itemResponse.getPaymentitems() == null)
                        continue;

                }catch (FeignException ex){
                    log.error("::Error getPayItem",ex.getLocalizedMessage());
                    ex.printStackTrace();
                    continue;
                }
                if(itemResponse.getPaymentitems().size() > 0)
                    createServiceProviderProduct(itemResponse.getPaymentitems(),biller,response);
            }

            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"QuickTeller product successfully updated",null);
        }catch (Exception ex){
            log.error("::Error create-QuickTeller-product {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to create QuickTeller biller product, try again later",null);
        }
    }


    private void createServiceProviderProduct(List<PaymentItem> itemList, ServiceProviderBiller providerBiller, AuthResponse response){
        try {
            List<ServiceProviderProduct> providerProductList = new ArrayList<>();
            for (PaymentItem item: itemList){
                try {
                    BigDecimal amount = new BigDecimal(item.getAmount());
                    if(amount.equals(BigDecimal.ZERO)){
                        amount = BigDecimal.ZERO;
                    }else {
                        amount = amount.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                    }
                    String name = providerBiller.getShortName() +" - "+ item.getPaymentitemname();
                    Optional<ServiceProviderProduct> providerProduct = serviceProviderProductRepository.findByNameAndProductCodeAndServiceProviderBillerAndIsActiveAndIsDeleted(name,item.getPaymentCode(),providerBiller,true,false);
                    if(!providerProduct.isPresent()){
                        providerProduct = Optional.of(new ServiceProviderProduct());
                        providerProduct.get().setCreatedBy(response.getData().getEmail());
                    }
                    providerProduct.get().setHasBundle(false);
                    if(providerBiller.getServiceProviderCategory().getType().equals(BillCategoryName.international_airtime.name())||
                            providerBiller.getServiceProviderCategory().getType().equals(BillCategoryName.epin.name())||
                            providerBiller.getServiceProviderCategory().getType().equals(BillCategoryName.airtime.name())||
                            providerBiller.getServiceProviderCategory().getType().equals(BillCategoryName.databundle.name())){
                        providerProduct.get().setHasTokenValidation(Boolean.FALSE);
                    }else {
                        providerProduct.get().setHasTokenValidation(Boolean.TRUE);
                    }
                    providerProduct.get().setServiceProviderBiller(providerBiller);
                    providerProduct.get().setAmount(amount);
                    providerProduct.get().setProductCode(item.getPaymentCode());
                    providerProduct.get().setName(name);
                    providerProduct.get().setDescription(providerBiller.getName() +" - "+ item.getPaymentitemname());
                    providerProduct.get().setType(item.getPaymentCode());
                    providerProduct.get().setHasAddOns(Boolean.FALSE);
                    providerProduct.get().setModifiedAt(LocalDateTime.now());
                    providerProduct.get().setModifiedBy(response.getData().getEmail());
                    providerProductList.add(providerProduct.get()); //This neglected because bulk update is not affecting the actual field
//                    serviceProviderProductRepository.saveAndFlush(providerProduct.get());
                }catch (Exception ex){
                    log.error("::Error createQtProduct {}",ex.getLocalizedMessage());
                    continue;
                }
            }
            if(providerProductList.size() > 0)
                serviceProviderProductRepository.saveAll(providerProductList);
            return;
        }catch (Exception ex){
            log.error("::Error creatingQTProduct {}",ex.getLocalizedMessage());
            return;
        }
    }


    @Override
    public ApiResponse<?> verifyCustomerAccountNumberOrSmartCardOrMeterNumber(String type, String account, String categoryType) {
        try {
//            String paymentItemUrl = baseUrl + billerPaymentItemUrl;

//            GetBillerPaymentItemResponse paymentItemResponse;
//            try {
//                Map<String, String> headers = requestHeaders.getISWAuthSecurityHeaders(clientId,clientSecret,paymentItemUrl, HttpMethod.GET.toString());
//                paymentItemResponse = quickTellerProxy.getBillerPaymentItems(type,getAuthorisation(headers),getSignature(headers),getNonce(headers),getTimeStamp(headers),getSignatureMethod(headers),providerTerminalId);
//                log.info("::PaymentItemResponse {}",paymentItemResponse);
//                if(paymentItemResponse.getPaymentitems() == null)
//                    return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to generate payment invoice, try again later",null);
//            }catch (FeignException ex){
//                log.error("::Error Generate Payment Info {}",ex.getLocalizedMessage());
//                String msg = ex.contentUTF8();
//                int status = ex.status();
//                if(msg != null && msg !="")
//                    return new ApiResponse<>(false,status,msg,null);
//
//                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Fail to generate payment invoice, try gain later",null);
//            }

//            String paymentCode= "";
//            if(paymentItemResponse.getPaymentitems().size() > 0){
//                paymentCode = paymentItemResponse.getPaymentitems().get(0).getPaymentCode();
//            }
            String validateUrl = baseUrl + customerValidationUrl;
            QuickTellerCustomerValidationResponse validationResponse;
            try {
                QuickTellerCustomerValidationRequest validationRequest = new QuickTellerCustomerValidationRequest();
                ValidationRequest request = new ValidationRequest();
                request.setCustomerId(account);
                request.setPaymentCode(type);
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



    @Override
    public ApiResponse<?> makeBillsPaymentRequest(SendPaymentAdviceRequest request , String categoryType) {
        try {
            SendPaymentAdviceResponse paymentAdviceResponse;
            try {
                String paymentUrl = baseUrl + sendPaymentAdviceUrl;
                Map<String, String> headers = requestHeaders.getISWAuthSecurityHeaders(clientId,clientSecret,paymentUrl, HttpMethod.GET.toString());
                paymentAdviceResponse = quickTellerProxy.sendPaymentAdvice(request,getAuthorisation(headers),getSignature(headers),getNonce(headers),getTimeStamp(headers),getSignatureMethod(headers),providerTerminalId);
                if(!paymentAdviceResponse.getResponseCode().equals(QUICK_TELLER_SUCCESSFUL)){
                    if(paymentAdviceResponse.getError() != null){
                        return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,paymentAdviceResponse.getError().getMessage(),null);
                    }else if(paymentAdviceResponse.getErrors() != null && paymentAdviceResponse.getErrors().size() > 0){
                        return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,paymentAdviceResponse.getErrors().get(0).getMessage(),null);
                    }else {
                        return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process "+categoryType+" payment, try again",null);
                    }
                }
            }catch (FeignException ex){
                log.error("::Error sendPaymentAdviceResponse {}",ex.getLocalizedMessage());
                String msg = ex.contentUTF8();
                int status = ex.status();
                String message = formatErrorMessage(msg);
                if(message != null && message != "")
                    return new ApiResponse<>(false,status,message,null);
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Fail to process your "+categoryType+" bill payment, try again later",null);
            }

            ApiResponse<?> tsq =  verifyQuickTellerTransactionRef(request.getRequestReference());
            if(tsq.getCode().equals(ApiResponse.Code.SUCCESS)){
                QueryTransactionResponse queryTransaction = objectMapper.convertValue(tsq.getData(),QueryTransactionResponse.class);
                GeneralPaymentResponseDto paymentResponseDto = generalPaymentResponse(queryTransaction,paymentAdviceResponse);
                return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,paymentAdviceResponse.getResponseMessage(),paymentResponseDto);
            }else {
                return tsq;
            }
//            return new ApiResponse<>(true, ApiResponse.Code.SUCCESS,"Success",paymentAdviceResponse);
        }catch (Exception ex){
            log.error("::Error QTmakeBillsPaymentRequest {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Fail to process your bill payment, try again later",null);
        }
    }


    private GeneralPaymentResponseDto generalPaymentResponse(QueryTransactionResponse transaction,SendPaymentAdviceResponse adviceResponse){
        try {
            GeneralPaymentResponseDto paymentResponseDto = new GeneralPaymentResponseDto();
            paymentResponseDto.setProviderReference(transaction.getTransactionRef());
            paymentResponseDto.setExchangeReference(transaction.getTransactionRef());
            paymentResponseDto.setStatusCode(transaction.getResponseCode());
            paymentResponseDto.setTransactionReference(transaction.getRequestReference());
            paymentResponseDto.setTransactionMessage(transaction.getTransactionSet());
            if(adviceResponse.getRechargePIN() != null && !adviceResponse.getRechargePIN().isEmpty()){
                paymentResponseDto.setVoucherCode(adviceResponse.getRechargePIN());
                paymentResponseDto.setTokenCode(adviceResponse.getRechargePIN());
                paymentResponseDto.setCreditToken(adviceResponse.getRechargePIN());
            }else {
                paymentResponseDto.setVoucherCode(transaction.getServiceCode());
                paymentResponseDto.setTokenCode(transaction.getServiceCode());
                paymentResponseDto.setCreditToken(transaction.getServiceCode());
            }
            if(transaction.getRecharge() != null){
                paymentResponseDto.setPurchasedPackage(transaction.getRecharge().getBiller());
                paymentResponseDto.setTransactionMessage(transaction.getTransactionSet() +" - "+paymentResponseDto.getPurchasedPackage());
            }else if(transaction.getBillPayment() != null){
                paymentResponseDto.setPurchasedPackage(transaction.getBillPayment().getBiller());
                paymentResponseDto.setTransactionMessage(transaction.getTransactionSet() +" - "+paymentResponseDto.getPurchasedPackage());
            }
            paymentResponseDto.setStatusMessage(transaction.getStatus());
            paymentResponseDto.setTokenAmount(new BigDecimal(transaction.getAmount()));
            paymentResponseDto.setTransactionNumber(transaction.getRequestReference());
            return paymentResponseDto;
        }catch (Exception ex){
            log.error("::Error generalPaymentResponse {}",ex.getLocalizedMessage());
            return null;
        }
    }




    private ApiResponse<?> verifyQuickTellerTransactionRef(String reference) {
        try {
            QueryTransactionResponse transactionResponse;
            String paymentUrl = baseUrl + queryTransactionUrl +"?requestreference="+reference;
            log.info("TRANS-URL {}",paymentUrl);
            Map<String, String> headers = requestHeaders.getISWAuthSecurityHeaders(clientId,clientSecret,paymentUrl, HttpMethod.GET.toString());
            try {
                transactionResponse = quickTellerProxy.getQueryTransaction(reference,getAuthorisation(headers),getSignature(headers),getNonce(headers),getTimeStamp(headers),getSignatureMethod(headers),providerTerminalId);
                if(!transactionResponse.getResponseCode().equals(QUICK_TELLER_SUCCESSFUL))
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Query",transactionResponse);
            }catch (FeignException ex){
                log.error("::Error QuickTellerTransactionRef {}",ex.getLocalizedMessage());
                String msg = ex.contentUTF8();
                int status = ex.status();
                String message = formatErrorMessage(msg);
                if(message != null && message != "")
                    return new ApiResponse<>(false,status,message,null);
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Fail to query bill transaction",null);
            }
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Success",transactionResponse);
        }catch (Exception ex){
            log.error("::Error verifyQuickTellerTransactionRef {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Fail to fetch transaction",null);
        }
    }


    @Override
    public ApiResponse<?> verifyQuickTellerTransactionRefAdmin(String token, String reference) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            QueryTransactionResponse transactionResponse;
            String paymentUrl = baseUrl + queryTransactionUrl +"?requestreference="+reference;
            log.info("TRANS-URL {}",paymentUrl);
            Map<String, String> headers = requestHeaders.getISWAuthSecurityHeaders(clientId,clientSecret,paymentUrl, HttpMethod.GET.toString());
            try {
                transactionResponse = quickTellerProxy.getQueryTransaction(reference,getAuthorisation(headers),getSignature(headers),getNonce(headers),getTimeStamp(headers),getSignatureMethod(headers),providerTerminalId);
                if(!transactionResponse.getResponseCode().equals(QUICK_TELLER_SUCCESSFUL))
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Query",transactionResponse);
            }catch (FeignException ex){
                log.error("::Error verifyQuickTellerTransactionRefAdmin {}",ex.getLocalizedMessage());
                String msg = ex.contentUTF8();
                int status = ex.status();
                String message = formatErrorMessage(msg);
                if(message != null && message != "")
                    return new ApiResponse<>(false,status,message,null);
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Fail to query bill transaction",null);
            }
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Success",transactionResponse);
        }catch (Exception ex){
            log.error("::Error verifyQuickTellerTransactionRefAdmin {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Fail to fetch transaction",null);
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

            if(name.contains("cable") || name.contains("cable tv"))
                return BillCategoryName.cabletv.name();

            if(name.contains("pay tv"))
                return BillCategoryName.pay_tv_subscription.name();//pay_tv_subscription

            if(name.contains("utility") || name.contains("utility bills") || name.contains("utilities"))
                return BillCategoryName.electricity.name();

            if(name.contains("mobile recharge") && name.contains("recharge")) //Mobile Recharge
                return BillCategoryName.airtime.name();

            if(name.contains("phone bills") || name.contains("airtime and data"))
                return BillCategoryName.databundle.name();

            if(name.contains("subscriptions") || name.contains("internet services"))
                return BillCategoryName.subscription.name();

            if(name.contains("embassy") || name.contains("embassies"))
                return BillCategoryName.embassy.name();

            if(name.contains("tax"))
                return BillCategoryName.tax.name();

            if(name.contains("donations") || name.contains("aid grants and donations"))
                return BillCategoryName.donation.name();

            if(name.contains("insurance") || name.contains("insuredirect"))
                return BillCategoryName.insurance.name();

            if(name.contains("financial services")) //Insurance && Investment
                return BillCategoryName.insurance_and_investment.name();

            if(name.contains("airlines") || name.contains("travel and hotel"))
                return BillCategoryName.airline.name();

            if(name.contains("transport") || name.contains("transport and toll payments"))
                return BillCategoryName.transport.name();

            if(name.contains("school and exam fees"))
                return BillCategoryName.education.name();

            if(name.contains("schoolboard"))
                return BillCategoryName.schoolboard.name();

            if(name.contains("shopping"))
                return BillCategoryName.shopping.name();

            if(name.contains("event tickets"))
                return BillCategoryName.event_ticket.name();

            if(name.contains("online shopping"))
                return BillCategoryName.online_shopping.name();

            if(name.contains("government payments"))
                return BillCategoryName.government_payments.name();

            if(name.contains("lagos state cbs"))
                return BillCategoryName.lagos_state_cbs.name();//Lagos State CBS

            if(name.contains("international airtime"))
                return BillCategoryName.international_airtime.name();//International Airtime

            if(name.contains("credit and loans"))
                return BillCategoryName.credit_and_loan_repayment.name(); // credit and loan repayment

            if(name.contains("religious institutions"))
                return BillCategoryName.religious_institutions.name();//Religious Institutions

            if(name.contains("nestle distributors"))
                return BillCategoryName.nestle_distributors.name();//Nestle Distributors

            if(name.contains("black friday"))
                return BillCategoryName.black_friday.name();//Nestle Distributors

            if(name.contains("apm terminals"))
                return BillCategoryName.apm_terminals.name();//APM Terminals //

            if(name.contains("dealer payments"))
                return BillCategoryName.dealer_payments.name();//Dealer Payments

            return name;
        }catch (Exception ex){
            log.error("::Error formatAndMatchCategory {}",ex.getLocalizedMessage());
            return null;
        }
    }

    private String formatName(String name){
        String billerName;
        if(name.toLowerCase().contains("utility") || name.toLowerCase().contains("utilities")){
            billerName = "Power / Electricity / Water / Utility";
        }else {
            billerName = name;
        }
        return billerName;
    }

    private String formatErrorMessage(String message){
        try {
            if(message.contains("error")){
                JsonNode jsonNode = objectMapper.readTree(message);
                String msg = jsonNode.get("error").get("message").asText();
                return  msg;
            }
            return null;
        }catch (Exception ex){
            log.error("::Error formatErrorMessage {}",ex.getLocalizedMessage());
            return null;
        }
    }


    private String unProcessQuickTellerProductionCategory(){
        String categories = "Invoice Payments, Blackberry, Mobile Money Wallets, Products and Services, " +
                "VF Global Services, PayChoice, Receive Money, " +
                "Commerce Retail Trade, Associations and Societies, BankOne MFBs, Cross River State Govt, " +
                "Send and Receive Money, Prepaid Card Services, Merchant Payment, Dues and Service Charge, " +
                "SmartMicro, Eminent Technologies, Merchant Payments, Interswitch SME, Airtel Data, " +
                "Airtime Top-up, Interswitch Services, Merchant Services";
        String lowerCase = categories.toLowerCase();
        return lowerCase;
    }

    private String unProcessQuickTellerTestCategory(){
        String categories = "Receive Money, Transfer Money, Quickteller Business, PayChoice, Mobile Money, Microfinance, Funds Transfer, Products and Services";
        String lowerName = categories.toLowerCase();
        return lowerName;
    }
}
