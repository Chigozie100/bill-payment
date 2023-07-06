package com.wayapay.thirdpartyintegrationservice.v2.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapay.thirdpartyintegrationservice.annotations.AuditPaymentOperation;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.FundTransferResponse;
import com.wayapay.thirdpartyintegrationservice.v2.service.notification.NotificationService;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.AuthResponse;
import com.wayapay.thirdpartyintegrationservice.util.*;
import com.wayapay.thirdpartyintegrationservice.v2.dto.BillCategoryName;
import com.wayapay.thirdpartyintegrationservice.v2.dto.BillCategoryType;
import com.wayapay.thirdpartyintegrationservice.v2.dto.PaymentStatus;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.*;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.GeneralPaymentResponseDto;
import com.wayapay.thirdpartyintegrationservice.v2.entity.*;
import com.wayapay.thirdpartyintegrationservice.v2.proxyclient.AuthProxy;
import com.wayapay.thirdpartyintegrationservice.v2.proxyclient.WalletProxy;
import com.wayapay.thirdpartyintegrationservice.v2.repository.*;
import com.wayapay.thirdpartyintegrationservice.v2.service.BillPaymentService;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.BaxiProxy;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.BaxiService;
import com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.QuickTellerService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service @RequiredArgsConstructor @Slf4j
public class CustomerBillPaymentServiceImpl implements BillPaymentService {

    private final CategoryRepository categoryRepository;
//    private final BillerCategoryRepository billerCategoryRepository;
//    private final BillerProductRepository billerProductRepository;
//    private final BillerProductBundleRepository billerProductBundleRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceProviderBillerRepository serviceProviderBillerRepository;
    private final ServiceProviderCategoryRepository serviceProviderCategoryRepository;
    private final ServiceProviderProductRepository serviceProviderProductRepository;
    private final ServiceProviderProductBundleRepository serviceProviderProductBundleRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final BillProviderChargeRepository billProviderChargeRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final AuthProxy authProxy;
    private final BaxiProxy baxiProxy;
    private final BaxiService baxiService;
    private final WalletProxy walletProxy;
    private final QuickTellerService quickTellerService;

    @Value("${app.config.baxi.agent-code}")
    private String baxiAgentCode;

    @Override
    public ApiResponse<?> fetchAllBillCategory(String token) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            List<Category> categoryList = categoryRepository.findAllByIsActiveAndIsDeleted(true, false);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Category fetched...",categoryList);
        }catch (Exception ex){
            log.error("::Error fetchAllBillCategory {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, try again later",null);
        }
    }

    @Override
    public ApiResponse<?> verifyCustomerAccountOrToken(String token, ValidateCustomerToken customerToken) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(customerToken.getServiceProviderId(), true,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Oops!\n Biller not available",null);

            Optional<ServiceProviderCategory> category = serviceProviderCategoryRepository.findByIdAndServiceProviderAndIsActiveAndIsDeleted(customerToken.getServiceProviderCategoryId(), serviceProvider.get(),true,false);
            if(!category.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Oops!\n Product not available",null);

            if(serviceProvider.get().getName().toLowerCase().startsWith("baxi")){
                ApiResponse<?> baxiResp = baxiService.verifyCustomerAccountNumberOrSmartCardOrMeterNumber(customerToken.getType(), customerToken.getAccount(), category.get().getType());
                return baxiResp;
            }else if(serviceProvider.get().getName().toLowerCase().startsWith("quick")){
                ApiResponse<?> quickTellerResp =  quickTellerService.verifyCustomerAccountNumberOrSmartCardOrMeterNumber(customerToken.getType(), customerToken.getAccount(), category.get().getType());
                return quickTellerResp;
            }else {
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to verify your customer token due to service provider unavailable",null);
            }
        }catch (Exception ex){
            log.error("::Error verifyCustomerAccountOrToken {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, try again later",null);
        }
    }


    @Override
    public ApiResponse<?> fetchBillersByCategory(String token, Long categoryId) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<Category> category = categoryRepository.findByIdAndIsActiveAndIsDeleted(categoryId,true,false);
            if(!category.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Oops!\n Biller category not found",null);

            Long serviceProviderId = fetchActiveProvider(BillCategoryName.valueOf(category.get().getName()));
            if(serviceProviderId == null)
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Oops!\n Service provider not available",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId, true,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Oops!\n Biller service provider not available",null);

            Optional<ServiceProviderCategory> serviceProviderCategory = serviceProviderCategoryRepository.findByTypeAndServiceProviderAndIsActiveAndIsDeleted(category.get().getName(),serviceProvider.get(),true,false);
            if(!serviceProviderCategory.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Oops!\n Biller category not found, can not process "+category.get().getName(),null);

            List<ServiceProviderBiller> billerRepositoryList = serviceProviderBillerRepository.findAllByServiceProviderIdAndServiceProviderCategoryAndIsActiveAndIsDeleted(serviceProviderId,serviceProviderCategory.get(),true,false);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Success",billerRepositoryList);
        }catch (Exception ex){
            log.error("::Error fetchBillerByCategory {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process your bill payment request, try again later",null);
        }
    }

    @Override
    public ApiResponse<?> fetchAllProductByBiller(String token, Long serviceProviderBillerId) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProviderBiller> serviceProviderBiller = serviceProviderBillerRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderBillerId,true,false);
            if(!serviceProviderBiller.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Oops!\n Biller not found, can not process your request",null);

            List<ServiceProviderProduct> serviceProviderProductList = serviceProviderProductRepository.findAllByServiceProviderBillerAndIsActiveAndIsDeleted(serviceProviderBiller.get(),true,false);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Success",serviceProviderProductList);
        }catch (Exception ex){
            log.error("::Error fetchAllProductByBiller {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process your bill payment request, try again later",null);
        }
    }

    @Override
    public ApiResponse<?> fetchAllBundleByProduct(String token, Long serviceProviderProductId) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProviderProduct> serviceProviderProduct = serviceProviderProductRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderProductId,true,false);
            if(!serviceProviderProduct.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Oops!\n Product not available, can not process your request",null);

            List<ServiceProviderProductBundle> bundleList = serviceProviderProductBundleRepository.findAllByServiceProviderProductAndIsActiveAndIsDeleted(serviceProviderProduct.get(),true,false);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Success",bundleList);
        }catch (Exception ex){
            log.error("::Error fetchAllBundleByProduct {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process your bill payment request, try again later",null);
        }
    }


    @Override @Transactional
    public ApiResponse<?> makeElectricityPayment(String token,Long serviceProviderBillerId, Long serviceProviderId, ElectricityPaymentDto electricityPaymentDto,String userAccountNumber,String pin) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true ,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider not available",null);

            Optional<ServiceProviderBiller> serviceProviderBiller = serviceProviderBillerRepository.findByIdAndServiceProviderIdAndIsActiveAndIsDeleted(serviceProviderBillerId,serviceProvider.get().getId(),true,false);
            if(!serviceProviderBiller.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider biller not available",null);

            Optional<BillProviderCharge> providerCharge = billProviderChargeRepository.findByServiceProviderCategoryAndServiceProviderAndIsActiveAndIsDeleted(serviceProviderBiller.get().getServiceProviderCategory(),serviceProvider.get(),true,false);
            if(!providerCharge.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Bill charges not found",null);

            if(serviceProvider.get().getName().toLowerCase().startsWith("baxi")){

                BigDecimal consumerFee = providerCharge.get().getConsumerCharges();
                BigDecimal billerFee = providerCharge.get().getBillerCharges();
                BigDecimal secureAmount= electricityPaymentDto.getAmount().add(consumerFee);
                BigDecimal secureAmountSentToBiller = electricityPaymentDto.getAmount().add(billerFee);
                String paymentReferenceNumber = baxiBillPaymentReferenceNumber();
                String eventId = fetchBillEventId(serviceProvider.get().getName());

                NewWalletResponse  newWalletResponse = fetchUserAccountDetail(userAccountNumber, token, false);
                if(newWalletResponse == null)
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Debit account not found",null);

                if (secureFund(serviceProvider.get().getName(),secureAmount,response.getData().getId(),userAccountNumber,paymentReferenceNumber,token,pin,eventId,BillCategoryType.UTILITY,newWalletResponse.getClr_bal_amt(),newWalletResponse.getAcct_name())) {
                    TransactionDto transactionDto = new TransactionDto();
                    transactionDto.setAmount(electricityPaymentDto.getAmount());
                    transactionDto.setBillerFee(billerFee);
                    transactionDto.setAccountNumber(userAccountNumber);
                    transactionDto.setConsumerFee(consumerFee);
                    transactionDto.setSenderName(newWalletResponse.getAcct_name());
                    transactionDto.setServiceProviderProductBundle(null);
                    transactionDto.setServiceProviderBiller(serviceProviderBiller.get());
                    try {
                        ApiResponse<?> paymentResponse = baxiService.requestElectricityPayment(electricityPaymentDto.getAmount(),electricityPaymentDto.getType(),electricityPaymentDto.getPhone(),electricityPaymentDto.getAccount(),paymentReferenceNumber);
                        if(paymentResponse.getCode().equals(ApiResponse.Code.SUCCESS)){
                            GeneralPaymentResponseDto paymentResponseDto = objectMapper.convertValue(paymentResponse.getData(),GeneralPaymentResponseDto.class);
                            log.info(":::paymentResponseDto {}",paymentResponseDto);

                            transactionDto.setServiceProviderReferenceNumber(paymentResponseDto.getProviderReference());
                            transactionDto.setNarration(paymentResponseDto.getTransactionMessage());
                            if(paymentResponseDto.getTokenCode() != null)
                                transactionDto.setCustomerDataToken(paymentResponseDto.getTokenCode());

                            log.info("::TransactionDto {}",transactionDto);
                            TransactionHistory history = saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.SUCCESSFUL,BillCategoryName.electricity);
                            //Todo: Push sms/email request
                            CompletableFuture.runAsync(() -> {
                                try {
                                    notificationService.pushSMSv2(history, token,response.getData().getEmail(), electricityPaymentDto.getPhone());
                                } catch (ThirdPartyIntegrationException e) {
                                    log.error("::Error pushSMSv2 {}", e.getLocalizedMessage());
                                }
                            });
                            return paymentResponse;
                        }
                        saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.FAILED,BillCategoryName.electricity);
                        return paymentResponse;
                    } catch (Exception ex) {
                        log.error(":::Error billspayment :: " + ex.getLocalizedMessage());
                        saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.ERROR,BillCategoryName.electricity);
                        throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
                    }
                }
                log.error("Unable to secure fund from user's account");
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
            }else if(serviceProvider.get().getName().toLowerCase().startsWith("quick")){
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Provider Not yet Supported",null);
            }else {
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Provider Not yet Supported",null);
            }
        }catch (Exception ex){
            log.error("::Error makeElectricityPayment {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,ex.getLocalizedMessage(),null);
        }
    }


    @Override @Transactional
    public ApiResponse<?> makeDataBundlePayment(String token,Long serviceProviderBundleId, Long serviceProviderId, DataBundlePaymentDto dataBundlePaymentDto,String userAccountNumber,String pin,Long serviceProviderBillerId) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true ,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider not available",null);

            Optional<ServiceProviderBiller> serviceProviderBiller = null;
            if(serviceProviderBillerId != null){
                serviceProviderBiller = serviceProviderBillerRepository.findByIdAndServiceProviderIdAndIsActiveAndIsDeleted(serviceProviderBillerId,serviceProvider.get().getId(), true,false);
                if(!serviceProviderBiller.isPresent())
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider biller not available",null);

            }

            Optional<ServiceProviderProductBundle> serviceProviderProductBundle = null;
            if(serviceProviderBundleId != null){
                serviceProviderProductBundle = serviceProviderProductBundleRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderBundleId,true,false);
                if(!serviceProviderProductBundle.isPresent())
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider product bundle not available",null);

            }

            ServiceProviderCategory serviceProviderCategory;
            if(serviceProviderBiller.isPresent()){
                serviceProviderCategory = serviceProviderBiller.get().getServiceProviderCategory();
            }else if(serviceProviderProductBundle.isPresent()){
                serviceProviderCategory = serviceProviderProductBundle.get().getServiceProviderProduct().getServiceProviderBiller().getServiceProviderCategory();
            }else {
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider biller/bundle not found",null);
            }

            Optional<BillProviderCharge> providerCharge = billProviderChargeRepository.findByServiceProviderCategoryAndServiceProviderAndIsActiveAndIsDeleted(serviceProviderCategory,serviceProvider.get(),true,false);
            if(!providerCharge.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Bill charges not found",null);

            if(serviceProvider.get().getName().toLowerCase().startsWith("baxi")){

                BigDecimal consumerFee = providerCharge.get().getConsumerCharges();
                BigDecimal billerFee = providerCharge.get().getBillerCharges();
                BigDecimal actualAmount = new BigDecimal(dataBundlePaymentDto.getAmount());
                BigDecimal secureAmount= actualAmount.add(consumerFee);
                BigDecimal secureAmountSentToBiller = actualAmount.add(billerFee);
                String paymentReferenceNumber = baxiBillPaymentReferenceNumber();
                String eventId = fetchBillEventId(serviceProvider.get().getName());

                NewWalletResponse  newWalletResponse = fetchUserAccountDetail(userAccountNumber, token, false);
                if(newWalletResponse == null)
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Debit account not found",null);

                if (secureFund(serviceProvider.get().getName(),secureAmount,response.getData().getId(),userAccountNumber,paymentReferenceNumber,token,pin,eventId,BillCategoryType.DATA_TOPUP,newWalletResponse.getClr_bal_amt(),newWalletResponse.getAcct_name())) {
                    TransactionDto transactionDto = new TransactionDto();
                    transactionDto.setAmount(new BigDecimal(dataBundlePaymentDto.getAmount()));
                    transactionDto.setBillerFee(billerFee);
                    transactionDto.setAccountNumber(userAccountNumber);
                    transactionDto.setConsumerFee(consumerFee);
                    transactionDto.setSenderName(newWalletResponse.getAcct_name());
                    transactionDto.setServiceProviderProductBundle(serviceProviderProductBundle.get());
                    transactionDto.setServiceProviderBiller(serviceProviderProductBundle.get().getServiceProviderProduct().getServiceProviderBiller());
                    try {
                        ApiResponse<?> paymentResponse = baxiService.requestDataBundlePayment(dataBundlePaymentDto.getProductCode(),dataBundlePaymentDto.getType(),dataBundlePaymentDto.getAmount(), dataBundlePaymentDto.getPhone(), paymentReferenceNumber);
                        if(paymentResponse.getCode().equals(ApiResponse.Code.SUCCESS)){
                            GeneralPaymentResponseDto paymentResponseDto = objectMapper.convertValue(paymentResponse.getData(),GeneralPaymentResponseDto.class);
                            log.info(":::Data bundle paymentResponseDto {}",paymentResponseDto);
                            transactionDto.setNarration(paymentResponseDto.getTransactionMessage());
                            transactionDto.setServiceProviderReferenceNumber(paymentResponseDto.getProviderReference());
                            if(paymentResponseDto.getVoucherCode() != null)
                                transactionDto.setCustomerDataToken(paymentResponseDto.getVoucherCode());
                            log.info("::TransactionDto {}",transactionDto);
                            TransactionHistory history = saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.SUCCESSFUL,BillCategoryName.databundle);
                            //Todo: Push sms/email request
                            CompletableFuture.runAsync(() -> {
                                try {
                                    notificationService.pushSMSv2(history, token,response.getData().getEmail(), dataBundlePaymentDto.getPhone());
                                } catch (ThirdPartyIntegrationException e) {
                                    log.error("::Error pushSMSv2 {}", e.getLocalizedMessage());
                                }
                            });
                            return paymentResponse;
                        }
                        saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.FAILED,BillCategoryName.databundle);
                        return paymentResponse;
                    } catch (Exception ex) {
                        log.error(":::Error data bundle {}",ex.getLocalizedMessage());
                        saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.ERROR,BillCategoryName.databundle);
                        throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
                    }
                }
                log.error("Unable to secure fund from user's account");
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
            }else if(serviceProvider.get().getName().toLowerCase().startsWith("quick")){
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Provider Not yet Supported",null);
            }else {
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Provider Not yet Supported",null);
            }
        }catch (Exception ex){
            log.error("::Error makeDataBundlePayment {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,ex.getLocalizedMessage(),null);
        }
    }


    @Override @Transactional
    public ApiResponse<?> makeAirtimePayment(String token,Long serviceProviderBillerId, Long serviceProviderId, AirtimePaymentDto airtimePaymentDto,String userAccountNumber,String pin) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true ,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider not available",null);

            Optional<ServiceProviderBiller> serviceProviderBiller = serviceProviderBillerRepository.findByIdAndServiceProviderIdAndIsActiveAndIsDeleted(serviceProviderBillerId,serviceProvider.get().getId(),true,false);
            if(!serviceProviderBiller.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider biller not available",null);

            Optional<BillProviderCharge> providerCharge = billProviderChargeRepository.findByServiceProviderCategoryAndServiceProviderAndIsActiveAndIsDeleted(serviceProviderBiller.get().getServiceProviderCategory(),serviceProvider.get(),true,false);
            if(!providerCharge.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Bill charges not found",null);

            if(serviceProvider.get().getName().toLowerCase().startsWith("baxi")){

                BigDecimal consumerFee = providerCharge.get().getConsumerCharges();
                BigDecimal billerFee = providerCharge.get().getBillerCharges();
                BigDecimal actualAmount = new BigDecimal(airtimePaymentDto.getAmount());
                BigDecimal secureAmount= actualAmount.add(consumerFee);
                BigDecimal secureAmountSentToBiller = actualAmount.add(billerFee);
                String paymentReferenceNumber = baxiBillPaymentReferenceNumber();
                String eventId = fetchBillEventId(serviceProvider.get().getName());

                NewWalletResponse  newWalletResponse = fetchUserAccountDetail(userAccountNumber, token, false);
                if(newWalletResponse == null)
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Debit account not found",null);

                if (secureFund(serviceProvider.get().getName(),secureAmount,response.getData().getId(),userAccountNumber,paymentReferenceNumber,token,pin,eventId,BillCategoryType.AIRTIME_TOPUP,newWalletResponse.getClr_bal_amt(),newWalletResponse.getAcct_name())) {
                    TransactionDto transactionDto = new TransactionDto();
                    transactionDto.setAmount(new BigDecimal(airtimePaymentDto.getAmount()));
                    transactionDto.setBillerFee(billerFee);
                    transactionDto.setAccountNumber(userAccountNumber);
                    transactionDto.setConsumerFee(consumerFee);
                    transactionDto.setSenderName(newWalletResponse.getAcct_name());
                    transactionDto.setServiceProviderProductBundle(null);
                    transactionDto.setServiceProviderBiller(serviceProviderBiller.get());
                    try {
                        ApiResponse<?> paymentResponse = baxiService.requestAirtimePayment(airtimePaymentDto.getAmount(),airtimePaymentDto.getPlan(), airtimePaymentDto.getPhone(), paymentReferenceNumber, airtimePaymentDto.getType());
                        if(paymentResponse.getCode().equals(ApiResponse.Code.SUCCESS)){
                            GeneralPaymentResponseDto paymentResponseDto = objectMapper.convertValue(paymentResponse.getData(),GeneralPaymentResponseDto.class);
                            log.info(":::Airtime paymentResponseDto {}",paymentResponseDto);
                            transactionDto.setNarration(paymentResponseDto.getTransactionMessage());
                            transactionDto.setServiceProviderReferenceNumber(paymentResponseDto.getProviderReference());
                            log.info("::TransactionDto {}",transactionDto);
                            TransactionHistory history = saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.SUCCESSFUL,BillCategoryName.airtime);
                            //Todo: Push sms/email request
                            CompletableFuture.runAsync(() -> {
                                try {
                                    notificationService.pushSMSv2(history, token,response.getData().getEmail(), airtimePaymentDto.getPhone());
                                } catch (ThirdPartyIntegrationException e) {
                                    log.error("::Error pushSMSv2 {}", e.getLocalizedMessage());
                                }
                            });
                            return paymentResponse;
                        }
                        saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.FAILED,BillCategoryName.airtime);
                        return paymentResponse;
                    } catch (Exception ex) {
                        log.error(":::Error Airtime {}",ex.getLocalizedMessage());
                        saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.ERROR,BillCategoryName.airtime);
                        throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
                    }
                }
                log.error("Unable to secure fund from user's account");
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
            }else if(serviceProvider.get().getName().toLowerCase().startsWith("quick")){
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Provider Not yet Supported",null);
            }else {
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Provider Not yet Supported",null);
            }
        }catch (Exception ex){
            log.error("::Error makeAirtimePayment {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,ex.getLocalizedMessage(),null);
        }
    }


    @Override @Transactional
    public ApiResponse<?> makeEpinPayment(String token,Long serviceProviderBundleId, Long serviceProviderId, EpinPaymentDto epinPaymentDto,String userAccountNumber, String pin,Long serviceProviderBillerId) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true ,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider not available",null);

            Optional<ServiceProviderBiller> serviceProviderBiller = null;
            if(serviceProviderBillerId != null){
                serviceProviderBiller = serviceProviderBillerRepository.findByIdAndServiceProviderIdAndIsActiveAndIsDeleted(serviceProviderBillerId,serviceProvider.get().getId(), true,false);
                if(!serviceProviderBiller.isPresent())
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider biller not available",null);

            }

            Optional<ServiceProviderProductBundle> serviceProviderProductBundle = null;
            if(serviceProviderBundleId != null){
                serviceProviderProductBundle = serviceProviderProductBundleRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderBundleId,true,false);
                if(!serviceProviderProductBundle.isPresent())
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider product bundle not available",null);

            }

            ServiceProviderCategory serviceProviderCategory;
            if(serviceProviderBiller.isPresent()){
                serviceProviderCategory = serviceProviderBiller.get().getServiceProviderCategory();
            }else if(serviceProviderProductBundle.isPresent()){
                serviceProviderCategory = serviceProviderProductBundle.get().getServiceProviderProduct().getServiceProviderBiller().getServiceProviderCategory();
            }else {
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider biller/bundle not found",null);
            }

            Optional<BillProviderCharge> providerCharge = billProviderChargeRepository.findByServiceProviderCategoryAndServiceProviderAndIsActiveAndIsDeleted(serviceProviderCategory,serviceProvider.get(),true,false);
            if(!providerCharge.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Bill charges not found",null);

            if(serviceProvider.get().getName().toLowerCase().startsWith("baxi")){

                BigDecimal consumerFee = providerCharge.get().getConsumerCharges();
                BigDecimal billerFee = providerCharge.get().getBillerCharges();
                BigDecimal actualAmount = new BigDecimal(epinPaymentDto.getAmount());
                BigDecimal secureAmount= actualAmount.add(consumerFee);
                BigDecimal secureAmountSentToBiller = actualAmount.add(billerFee);
                String paymentReferenceNumber = baxiBillPaymentReferenceNumber();
                String eventId = fetchBillEventId(serviceProvider.get().getName());

                NewWalletResponse  newWalletResponse = fetchUserAccountDetail(userAccountNumber, token, false);
                if(newWalletResponse == null)
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Debit account not found",null);

                if (secureFund(serviceProvider.get().getName(),secureAmount,response.getData().getId(),userAccountNumber,paymentReferenceNumber,token,pin,eventId,BillCategoryType.UTILITY,newWalletResponse.getClr_bal_amt(),newWalletResponse.getAcct_name())) {
                    TransactionDto transactionDto = new TransactionDto();
                    transactionDto.setAmount(new BigDecimal(epinPaymentDto.getAmount()));
                    transactionDto.setBillerFee(billerFee);
                    transactionDto.setAccountNumber(userAccountNumber);
                    transactionDto.setConsumerFee(consumerFee);
                    transactionDto.setSenderName(newWalletResponse.getAcct_name());
                    transactionDto.setServiceProviderProductBundle(serviceProviderProductBundle.get());
                    transactionDto.setServiceProviderBiller(serviceProviderProductBundle.get().getServiceProviderProduct().getServiceProviderBiller());
                    try {
                        ApiResponse<?> paymentResponse = baxiService.requestEpinPayment(epinPaymentDto.getNumberOfPins(), epinPaymentDto.getAmount(), epinPaymentDto.getFixAmount(),epinPaymentDto.getType(),paymentReferenceNumber);
                        if(paymentResponse.getCode().equals(ApiResponse.Code.SUCCESS)){
                            GeneralPaymentResponseDto paymentResponseDto = objectMapper.convertValue(paymentResponse.getData(),GeneralPaymentResponseDto.class);
                            log.info(":::Epin paymentResponseDto {}",paymentResponseDto);
                            transactionDto.setNarration(paymentResponseDto.getTransactionMessage());
                            transactionDto.setServiceProviderReferenceNumber(paymentResponseDto.getProviderReference());
                            log.info("::TransactionDto {}",transactionDto);
                            TransactionHistory history = saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.SUCCESSFUL,BillCategoryName.epin);
                            //Todo: Push sms/email request
                            CompletableFuture.runAsync(() -> {
                                try {
                                    notificationService.pushSMSv2(history, token,response.getData().getEmail(), response.getData().getPhoneNumber());
                                } catch (ThirdPartyIntegrationException e) {
                                    log.error("::Error pushSMSv2 {}", e.getLocalizedMessage());
                                }
                            });
                            return paymentResponse;
                        }
                        saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.FAILED,BillCategoryName.epin);
                        return paymentResponse;
                    } catch (Exception ex) {
                        log.error(":::Error Epin bundle {}",ex.getLocalizedMessage());
                        saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.ERROR,BillCategoryName.epin);
                        throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
                    }
                }
                log.error("Unable to secure fund from user's account");
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
            }else if(serviceProvider.get().getName().toLowerCase().startsWith("quick")){
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Provider Not yet Supported",null);
            }else {
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Provider Not yet Supported",null);
            }
        }catch (Exception ex){
            log.error("::Error makeEpinPayment {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,ex.getLocalizedMessage(),null);
        }
    }


    @Override @Transactional
    public ApiResponse<?> makeCableTvPayment(String token, Long serviceProviderBundleId,Long serviceProviderId, CableTvPaymentDto cableTvPaymentDto,String userAccountNumber, String pin,Long serviceProviderBillerId) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true ,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider not available",null);

            Optional<ServiceProviderBiller> serviceProviderBiller = null;
            if(serviceProviderBillerId != null){
                serviceProviderBiller = serviceProviderBillerRepository.findByIdAndServiceProviderIdAndIsActiveAndIsDeleted(serviceProviderBillerId,serviceProvider.get().getId(), true,false);
                if(!serviceProviderBiller.isPresent())
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider biller not available",null);

            }

            Optional<ServiceProviderProductBundle> serviceProviderProductBundle = null;
            if(serviceProviderBundleId != null){
                serviceProviderProductBundle = serviceProviderProductBundleRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderBundleId,true,false);
                if(!serviceProviderProductBundle.isPresent())
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider product bundle not available",null);

            }

            ServiceProviderCategory serviceProviderCategory;
            if(serviceProviderBiller.isPresent()){
                serviceProviderCategory = serviceProviderBiller.get().getServiceProviderCategory();
            }else if(serviceProviderProductBundle.isPresent()){
                serviceProviderCategory = serviceProviderProductBundle.get().getServiceProviderProduct().getServiceProviderBiller().getServiceProviderCategory();
            }else {
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider biller/bundle not found",null);
            }

            Optional<BillProviderCharge> providerCharge = billProviderChargeRepository.findByServiceProviderCategoryAndServiceProviderAndIsActiveAndIsDeleted(serviceProviderCategory,serviceProvider.get(),true,false);
            if(!providerCharge.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Bill charges not found",null);

            if(serviceProvider.get().getName().toLowerCase().startsWith("baxi")){

                BigDecimal consumerFee = providerCharge.get().getConsumerCharges();
                BigDecimal billerFee = providerCharge.get().getBillerCharges();
                BigDecimal actualAmount = new BigDecimal(cableTvPaymentDto.getAmount());
                BigDecimal secureAmount= actualAmount.add(consumerFee);
                BigDecimal secureAmountSentToBiller = actualAmount.add(billerFee);
                String paymentReferenceNumber = baxiBillPaymentReferenceNumber();
                String eventId = fetchBillEventId(serviceProvider.get().getName());

                NewWalletResponse  newWalletResponse = fetchUserAccountDetail(userAccountNumber, token, false);
                if(newWalletResponse == null)
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Debit account not found",null);

                if (secureFund(serviceProvider.get().getName(),secureAmount,response.getData().getId(),userAccountNumber,paymentReferenceNumber,token,pin,eventId,BillCategoryType.CABLE,newWalletResponse.getClr_bal_amt(),newWalletResponse.getAcct_name())) {
                    TransactionDto transactionDto = new TransactionDto();
                    transactionDto.setAmount(new BigDecimal(cableTvPaymentDto.getAmount()));
                    transactionDto.setBillerFee(billerFee);
                    transactionDto.setAccountNumber(userAccountNumber);
                    transactionDto.setConsumerFee(consumerFee);
                    transactionDto.setSenderName(newWalletResponse.getAcct_name());
                    transactionDto.setServiceProviderProductBundle(serviceProviderProductBundle.get());
                    transactionDto.setServiceProviderBiller(serviceProviderProductBundle.get().getServiceProviderProduct().getServiceProviderBiller());
                    try {
                        ApiResponse<?> paymentResponse = baxiService.requestCableTvPayment(cableTvPaymentDto.getType(),cableTvPaymentDto.getPhone(),cableTvPaymentDto.getAmount(),paymentReferenceNumber,cableTvPaymentDto.getProductCode(),cableTvPaymentDto.getSmartCardNumber(),cableTvPaymentDto.getMonthPaidFor());
                        if(paymentResponse.getCode().equals(ApiResponse.Code.SUCCESS)){
                            GeneralPaymentResponseDto paymentResponseDto = objectMapper.convertValue(paymentResponse.getData(),GeneralPaymentResponseDto.class);
                            log.info(":::Cable Tv paymentResponseDto {}",paymentResponseDto);
                            transactionDto.setNarration(paymentResponseDto.getTransactionMessage());
                            transactionDto.setServiceProviderReferenceNumber(paymentResponseDto.getProviderReference());
                            log.info("::TransactionDto {}",transactionDto);
                            TransactionHistory history = saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.SUCCESSFUL,BillCategoryName.cabletv);
                            //Todo: Push sms/email request
                            CompletableFuture.runAsync(() -> {
                                try {
                                    notificationService.pushSMSv2(history, token,response.getData().getEmail(), cableTvPaymentDto.getPhone());
                                } catch (ThirdPartyIntegrationException e) {
                                    log.error("::Error pushSMSv2 {}", e.getLocalizedMessage());
                                }
                            });
                            return paymentResponse;
                        }
                        saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.FAILED,BillCategoryName.cabletv);
                        return paymentResponse;
                    } catch (Exception ex) {
                        log.error(":::Error Cable Tv bundle {}",ex.getLocalizedMessage());
                        saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.ERROR,BillCategoryName.cabletv);
                        throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
                    }
                }
                log.error("Unable to secure fund from user's account");
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
            }else if(serviceProvider.get().getName().toLowerCase().startsWith("quick")){
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Provider Not yet Supported",null);
            }else {
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Provider Not yet Supported",null);
            }
        }catch (Exception ex){
            log.error("::Error makeCableTvPayment {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,ex.getLocalizedMessage(),null);
        }
    }


    @Override @Transactional
    public ApiResponse<?> makeBettingPayment(String token,Long serviceProviderBillerId, Long serviceProviderId, BettingPaymentDto bettingPaymentDto,String userAccountNumber, String pin) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true ,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider not available",null);

            Optional<ServiceProviderBiller> serviceProviderBiller = serviceProviderBillerRepository.findByIdAndServiceProviderIdAndIsActiveAndIsDeleted(serviceProviderBillerId,serviceProvider.get().getId(),true,false);
            if(!serviceProviderBiller.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider biller not available",null);

            Optional<BillProviderCharge> providerCharge = billProviderChargeRepository.findByServiceProviderCategoryAndServiceProviderAndIsActiveAndIsDeleted(serviceProviderBiller.get().getServiceProviderCategory(),serviceProvider.get(),true,false);
            if(!providerCharge.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Bill charges not found",null);

            if(serviceProvider.get().getName().toLowerCase().startsWith("baxi")){

                BigDecimal consumerFee = providerCharge.get().getConsumerCharges();
                BigDecimal billerFee = providerCharge.get().getBillerCharges();
                BigDecimal actualAmount = bettingPaymentDto.getAmount();
                BigDecimal secureAmount= actualAmount.add(consumerFee);
                BigDecimal secureAmountSentToBiller = actualAmount.add(billerFee);
                String paymentReferenceNumber = baxiBillPaymentReferenceNumber();
                String eventId = fetchBillEventId(serviceProvider.get().getName());

                NewWalletResponse  newWalletResponse = fetchUserAccountDetail(userAccountNumber, token, false);
                if(newWalletResponse == null)
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Debit account not found",null);

                if (secureFund(serviceProvider.get().getName(),secureAmount,response.getData().getId(),userAccountNumber,paymentReferenceNumber,token,pin,eventId,BillCategoryType.BETTING,newWalletResponse.getClr_bal_amt(),newWalletResponse.getAcct_name())) {
                    TransactionDto transactionDto = new TransactionDto();
                    transactionDto.setAmount(bettingPaymentDto.getAmount());
                    transactionDto.setBillerFee(billerFee);
                    transactionDto.setAccountNumber(userAccountNumber);
                    transactionDto.setConsumerFee(consumerFee);
                    transactionDto.setSenderName(newWalletResponse.getAcct_name());
                    transactionDto.setServiceProviderProductBundle(null);
                    transactionDto.setServiceProviderBiller(serviceProviderBiller.get());
                    try {
                        ApiResponse<?> paymentResponse = baxiService.requestBettingPayment(bettingPaymentDto.getAmount(),bettingPaymentDto.getType(),paymentReferenceNumber,bettingPaymentDto.getAccountNumber());
                        if(paymentResponse.getCode().equals(ApiResponse.Code.SUCCESS)){
                            GeneralPaymentResponseDto paymentResponseDto = objectMapper.convertValue(paymentResponse.getData(),GeneralPaymentResponseDto.class);
                            log.info(":::Betting paymentResponseDto {}",paymentResponseDto);
                            transactionDto.setNarration(paymentResponseDto.getTransactionMessage());
                            transactionDto.setServiceProviderReferenceNumber(paymentResponseDto.getProviderReference());
                            log.info("::TransactionDto {}",transactionDto);
                            TransactionHistory history = saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.SUCCESSFUL,BillCategoryName.betting);
                            //Todo: Push sms/email request
                            CompletableFuture.runAsync(() -> {
                                try {
                                    notificationService.pushSMSv2(history, token,response.getData().getEmail(), response.getData().getPhoneNumber());
                                } catch (ThirdPartyIntegrationException e) {
                                    log.error("::Error pushSMSv2 {}", e.getLocalizedMessage());
                                }
                            });
                            return paymentResponse;
                        }
                        saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.FAILED,BillCategoryName.betting);
                        return paymentResponse;
                    } catch (Exception ex) {
                        log.error(":::Error Game betting {}",ex.getLocalizedMessage());
                        saveTransactionDetail(transactionDto,response.getData().getEmail(),paymentReferenceNumber, String.valueOf(response.getData().getId()),PaymentStatus.ERROR,BillCategoryName.betting);
                        throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
                    }
                }
                log.error("Unable to secure fund from user's account");
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
            }else if(serviceProvider.get().getName().toLowerCase().startsWith("quick")){
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Provider Not yet Supported",null);
            }else {
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Provider Not yet Supported",null);
            }
        }catch (Exception ex){
            log.error("::Error makeBettingPayment {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,ex.getLocalizedMessage(),null);
        }
    }

    @Override
    public ApiResponse<?> fetchBillTransactionByReference(String token, String reference) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<TransactionHistory> history = transactionHistoryRepository.findByPaymentReferenceNumberAndSenderUserIdAndIsActiveAndIsDeleted(reference, String.valueOf(response.getData().getId()),true,false);
            if(!history.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Transaction not found",null);

            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Success",history.get());
        }catch (Exception ex){
            log.error("::Error fetchBillTransactionByReference {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process your request, try again later",null);
        }
    }


    private Long fetchActiveProvider(BillCategoryName name){
        try {
            switch (name){
                case epin:
                    Optional<ServiceProvider> provider = serviceProviderRepository.findFirstByPrecedenceAndProcessEpinAndIsActiveAndIsDeleted(1,true,true,false);
                    if(provider.isPresent())
                        return provider.get().getId();
                    return null;
                case airtime:
                    Optional<ServiceProvider> airtime = serviceProviderRepository.findFirstByPrecedenceAndProcessAirtimeAndIsActiveAndIsDeleted(1,true,true,false);
                    if(airtime.isPresent())
                        return airtime.get().getId();
                    return null;
                case cabletv:
                    Optional<ServiceProvider> cableTv = serviceProviderRepository.findFirstByPrecedenceAndProcessCableTvAndIsActiveAndIsDeleted(1,true,true,false);
                    if(cableTv.isPresent())
                        return cableTv.get().getId();
                    return null;
                case databundle:
                    Optional<ServiceProvider> dataBundle = serviceProviderRepository.findFirstByPrecedenceAndProcessDataBundleAndIsActiveAndIsDeleted(1,true,true,false);
                    if(dataBundle.isPresent())
                        return dataBundle.get().getId();
                    return null;
                case electricity:
                    Optional<ServiceProvider> electricity = serviceProviderRepository.findFirstByPrecedenceAndProcessElectricityAndIsActiveAndIsDeleted(1,true,true,false);
                    if(electricity.isPresent())
                        return electricity.get().getId();
                    return null;
                case betting:
                    Optional<ServiceProvider> betting = serviceProviderRepository.findFirstByPrecedenceAndProcessBettingAndIsActiveAndIsDeleted(1,true,true,false);
                    if(betting.isPresent())
                        return betting.get().getId();
                    return null;
                case vehicle:
                    Optional<ServiceProvider> vehicle = serviceProviderRepository.findFirstByPrecedenceAndProcessGovernmentPaymentAndIsActiveAndIsDeleted(1,true,true,false);
                    if(vehicle.isPresent())
                        return vehicle.get().getId();
                    return null;
                case education:
                    Optional<ServiceProvider> education = serviceProviderRepository.findFirstByPrecedenceAndProcessSchoolFeesAndIsActiveAndIsDeleted(1,true,true,false);
                    if(education.isPresent())
                        return education.get().getId();
                    return null;
                case insurance:
                    Optional<ServiceProvider> insurance = serviceProviderRepository.findFirstByPrecedenceAndProcessInsuranceAndIsActiveAndIsDeleted(1,true,true,false);
                    if(insurance.isPresent())
                        return insurance.get().getId();
                    return null;
                default:
                    return null;
            }
        }catch (Exception ex){
            log.error("::Error fetchActiveProvider {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return null;
        }
    }


    @Transactional
    private TransactionHistory createTransactionHistory(TransactionDto transactionDto, String email, String referenceNumber, String userId,PaymentStatus status,BillCategoryName categoryName) {
        try {
            Optional<TransactionHistory> history = transactionHistoryRepository.findBySenderUserIdAndPaymentReferenceNumberAndCategoryName(userId,referenceNumber,categoryName);
            if(!history.isPresent()){
                history = Optional.of(new TransactionHistory());
                history.get().setModifiedBy(email);
                history.get().setCreatedBy(email);
                history.get().setSenderUserId(userId);
            }
            BeanUtils.copyProperties(transactionDto,history.get());
            history.get().setCategoryName(categoryName);
            history.get().setStatus(status);
            history.get().setPaymentReferenceNumber(referenceNumber);

            transactionHistoryRepository.save(history.get());
            log.info(":: createTransactionHistory {}",history.get());
            return history.get();
        }catch (Exception ex){
            log.error("::Error createTransactionHistory {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            TransactionHistory history = new TransactionHistory();
            history.setModifiedBy(email);
            history.setCreatedBy(email);
            history.setSenderUserId(userId);
            BeanUtils.copyProperties(transactionDto,history);
            history.setCategoryName(categoryName);
            history.setStatus(PaymentStatus.FAILED);
            transactionHistoryRepository.save(history);
            return history;
        }
    }


    private String fetchBillEventId(String providerName) {
        String eventId = "";
        if(providerName.toLowerCase().startsWith("quick")) {
            eventId = Constants.QUICKTELLER__INTRANSIT;
        }else if(providerName.toLowerCase().startsWith("baxi")) {
            eventId = Constants.BAXI_INTRANSIT;
        }
        return eventId;
    }


    private String baxiBillPaymentReferenceNumber(){
        String generatedString = RandomStringUtils.randomAlphanumeric(10);
        String baxiRef = "Baxi_"+generatedString;
        return baxiRef;
    }

    private String quickTellerBillPaymentReferenceNumber(){
        String generatedString = RandomStringUtils.randomAlphanumeric(10);
        String baxiRef = "QuickTeller_"+generatedString;
        return baxiRef;
    }


    @AuditPaymentOperation(stage = Stage.SECURE_FUND, status = Status.START)
    public boolean secureFund(String providerName, BigDecimal amount, Long userId, String userAccountNumber,
                              String transactionReference, String token, String pin, String eventId,
                              BillCategoryType billType,Double accountBalance,String accountName)
            throws ThirdPartyIntegrationException {

        processPayment( providerName,amount,userId,userAccountNumber,transactionReference,token,pin,eventId,billType,accountBalance,accountName);
        return true;
    }

    @AuditPaymentOperation(stage = Stage.SAVE_TRANSACTION_DETAIL, status = Status.END)
    public TransactionHistory saveTransactionDetail(TransactionDto transactionDto, String email, String referenceNumber,
                                                    String userId,PaymentStatus status,BillCategoryName categoryName) {
        TransactionHistory history = createTransactionHistory( transactionDto,  email, referenceNumber, userId, status, categoryName);
        return history;
    }


    private void checkAccountBalance(Double mainWalletResponse, BigDecimal amount) throws ThirdPartyIntegrationException {
        if (mainWalletResponse < amount.doubleValue())
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, Constants.INSUFFICIENT_FUND);
    }

    @AuditPaymentOperation(stage = Stage.SECURE_FUND, status = Status.START)
    public boolean secureFundAdmin(String providerName, BigDecimal amount, Long userId, String userAccountNumber, String transactionReference, String token, String pin, BillCategoryType billType, String eventId,Double accountBalance,String accountName)
            throws ThirdPartyIntegrationException {

        processPayment(providerName, amount,  userId,  userAccountNumber,  transactionReference,  token, pin, eventId,billType,accountBalance,accountName);
        return true;
    }


    private void processPayment(String providerName, BigDecimal amount, Long userId,String userAccountNumber,
                                String transactionReference, String token, String pin, String eventId,
                                BillCategoryType billType,Double accountBalance,String accountName)
            throws ThirdPartyIntegrationException {

//        NewWalletResponse mainWalletResponse2 = fetchUserAccountDetail(userAccountNumber, token, isAdmin);

        checkAccountBalance(accountBalance,amount);

        TransferFromWalletPojo trans = new TransferFromWalletPojo();
        trans.setAmount(amount);
        trans.setCustomerAccountNumber(userAccountNumber);
        trans.setEventId(eventId);
        trans.setPaymentReference(transactionReference);
        trans.setTranCrncy("NGN");
        trans.setTransactionCategory(billType.name());
        trans.setTranNarration(TransactionType.BILLS_PAYMENT.name());
        trans.setUserId(userId);
        trans.setSenderName(accountName);
        trans.setReceiverName(providerName);
        try {
            ResponseEntity<String> response = walletProxy.transferFromUserToWaya(trans,token, pin);
            log.info("::Transfer Response {}",response);
        } catch (FeignException ex) {
            log.error(":::Error processPayment {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, getErrorMessage(ex.contentUTF8()));
        }
    }


    public String getErrorMessage(String errorInJson) {
        try {
            return CommonUtils.getObjectMapper().readValue(errorInJson, FundTransferResponse.class).getMessage();
        } catch (JsonProcessingException e) {
            log.error(":::Error getErrorMessage {}", e.getLocalizedMessage());
            return Constants.ERROR_MESSAGE;
        }
    }

    private NewWalletResponse fetchUserAccountDetail(String userAccountNumber, String token, Boolean isAdmin){
        ResponseEntity<InfoResponse> responseEntity = walletProxy.getUserWalletByUser(userAccountNumber, token);
        InfoResponse infoResponse = responseEntity.getBody();
        return Objects.requireNonNull(infoResponse).data;
    }


}
