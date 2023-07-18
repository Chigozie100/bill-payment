package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.AuthResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.BillCategoryName;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.CustomerTokenValidationDto;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.FundTransferResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.GeneralPaymentResponseDto;
import com.wayapay.thirdpartyintegrationservice.v2.entity.*;
import com.wayapay.thirdpartyintegrationservice.v2.proxyclient.AuthProxy;
import com.wayapay.thirdpartyintegrationservice.v2.repository.*;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.BaxiProxy;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.BaxiService;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto.*;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.WordUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service @RequiredArgsConstructor @Slf4j
public class BaxiServiceImpl implements BaxiService {

    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceProviderBillerRepository serviceProviderBillerRepository;
    private final ServiceProviderCategoryRepository serviceProviderCategoryRepository;
    private final ServiceProviderProductRepository serviceProviderProductRepository;
    private final ServiceProviderProductBundleRepository serviceProviderProductBundleRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final AuthProxy authProxy;
    private final BaxiProxy baxiProxy;

    @Value("${app.config.baxi.x-api-key}")
    private String baxiApiKey;

    @Value("${app.config.baxi.agent-code}")
    private String baxiAgentCode;




    @Override @Transactional
    public ApiResponse<?> createBaxiServiceProviderCategory(String token, Long serviceProviderId) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Provider not found",null);

            CategoryResponse baxiCategoryList;
            try{
                baxiCategoryList = baxiProxy.fetchAllCategory(baxiApiKey);
                log.info("::baxiCategoryList {}",baxiCategoryList);
            }catch (FeignException ex){
                log.error("::Error BaxiCategory List {}",ex.getLocalizedMessage());
                String msg = ex.contentUTF8();
                int status = ex.status();
                return new ApiResponse<>(false, HttpStatus.valueOf(status).value(),msg,null);
            }

            List<ServiceProviderCategory> categoryList = new ArrayList<>();
            for (ServiceCategoryListDto category: baxiCategoryList.getData()){

                if(category.getServiceType().equalsIgnoreCase("payment") ||
                        category.getServiceType().equalsIgnoreCase("transfer") ||
                        category.getServiceType().equalsIgnoreCase("gaming")||
                        category.getServiceType().equalsIgnoreCase("mobile-money")||
                        category.getServiceType().equalsIgnoreCase("collections")){
                    continue;
                }
                Optional<ServiceProviderCategory> exist = serviceProviderCategoryRepository.findByServiceProviderAndTypeAndIsActiveAndIsDeleted(serviceProvider.get(),category.getServiceType(),true,false);
                if(exist.isPresent())
                    continue;

                ServiceProviderCategory providerCategory = new ServiceProviderCategory();
                providerCategory.setServiceProvider(serviceProvider.get());
                providerCategory.setName(category.getName());
                providerCategory.setType(category.getServiceType());
                providerCategory.setDescription(category.getName());
                providerCategory.setCreatedBy(response.getData().getEmail());
                providerCategory.setModifiedBy(response.getData().getEmail());
                categoryList.add(providerCategory);
            }
            serviceProviderCategoryRepository.saveAll(categoryList);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Successfully updated provider category",categoryList);
        }catch (Exception ex){
            log.error("::Error createBaxiServiceProviderCategory {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }


    @Override @Transactional
    public ApiResponse<?> createBaxiServiceProviderBiller(String token, Long serviceProviderId) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Baxi provider not found",null);

            List<ServiceProviderCategory> categoryList = serviceProviderCategoryRepository.findAllByServiceProviderAndIsActiveAndIsDeleted(serviceProvider.get(),true,false);
            if(categoryList.size() < 1)
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Baxi service provider category not found, please initiate provider category first",null);

            List<ServiceProviderBiller> serviceProviderBillerList = new ArrayList<>();
            for(ServiceProviderCategory category: categoryList){

                if(category.getType().equalsIgnoreCase("electricity")){

                    GetAllElectricityBillersByCategoryResponse billerProvidersElect;
                    try {
                        billerProvidersElect  = baxiProxy.getAllBillersByElectricityAndCategory(baxiApiKey,category.getType());
                        log.info("::BillerProvidersElectResp {0}, {1}",category.getType(), billerProvidersElect);
                    }catch (FeignException ex){
                        log.error("::Error billerProvidersElectricity List {}",ex.getLocalizedMessage());
                        String msg = ex.contentUTF8();
                        int status = ex.status();
                        log.error(":: Message {0}, {1}",msg,status);
                        continue;
                    }

                    List<ServiceProviderBiller> serviceBillerElectricityList = new ArrayList<>();
                    for (BillerDetail billerDetail: billerProvidersElect.getData()){

                        String name = getBillerServiceName(billerDetail, category.getType());
                        if(name == null)
                            continue;

                        log.info("RESP NAME:: {}",name);
                        Optional<ServiceProviderBiller> existingBiller = serviceProviderBillerRepository
                                .findByNameAndServiceProviderCategoryAndIsActiveAndIsDeleted(name,category,true,false);
                        if(!existingBiller.isPresent()){
                            existingBiller = Optional.of(new ServiceProviderBiller());
                            existingBiller.get().setServiceProviderCategory(category);
                            existingBiller.get().setCreatedBy(response.getData().getEmail());
                            existingBiller.get().setModifiedBy(response.getData().getEmail());
                            existingBiller.get().setName(name);
                            existingBiller.get().setDescription(billerDetail.getShortname());
                            existingBiller.get().setType(billerDetail.getService_type());
                        }
                        if(billerDetail.getName().contains("prepaid")){
                            existingBiller.get().setProductId(billerDetail.getProduct_id());
                            existingBiller.get().setBillerId(billerDetail.getBiller_id());
                            existingBiller.get().setIsPrepaid(Boolean.TRUE);
                            existingBiller.get().setPrepaidName(billerDetail.getService_type());
                        }else {
                            existingBiller.get().setEPostpaidBillerId(billerDetail.getBiller_id());
                            existingBiller.get().setEPostpaidProductId(billerDetail.getProduct_id());
                            existingBiller.get().setPostpaidName(billerDetail.getService_type());
                            existingBiller.get().setIsPostpaid(Boolean.TRUE);
                        }
                        existingBiller.get().setIsRequiredIdVerification(Boolean.TRUE);
                        existingBiller.get().setServiceProviderId(serviceProviderId);
                        serviceProviderBillerRepository.saveAndFlush(existingBiller.get());
//                        serviceBillerElectricityList.add(existingBiller.get());
                    }
//                    serviceProviderBillerList.addAll(serviceBillerElectricityList);
//                    serviceProviderBillerRepository.saveAll(serviceBillerElectricityList);
                }else if(category.getType().equalsIgnoreCase("betting")){

                    GetAllBillersByCategoryResponse billerProvidersElect;
                    try {
                        billerProvidersElect  = baxiProxy.getAllBillersByElectricityAndBettingCategory(baxiApiKey,category.getType());
                        log.info("::BillerProvidersElectResp {0}, {1}",category.getType(), billerProvidersElect);
                    }catch (FeignException ex){
                        log.error("::Error billerProvidersElect List {}",ex.getLocalizedMessage());
                        String msg = ex.contentUTF8();
                        int status = ex.status();
                        log.error(":: Message {0}, {1}",msg,status);
                        continue;
                    }

                    List<ServiceProviderBiller> serviceBillerElectricAndBettings = new ArrayList<>();
                    for (BillerDetail billerDetail: billerProvidersElect.getData().getProviders()){

                        String name = getBillerServiceName(billerDetail, category.getType());
                        if(name == null)
                            continue;

                        Optional<ServiceProviderBiller> betting = serviceProviderBillerRepository
                                .findByNameAndServiceProviderCategoryAndIsActiveAndIsDeleted(name,category,true,false);
                        if(betting.isPresent())
                            continue;

                        ServiceProviderBiller eBiller = new ServiceProviderBiller();
                        eBiller.setServiceProviderCategory(category);
                        eBiller.setCreatedBy(response.getData().getEmail());
                        eBiller.setModifiedBy(response.getData().getEmail());
                        eBiller.setProductId(billerDetail.getProduct_id());
                        eBiller.setName(name);
                        eBiller.setType(billerDetail.getService_type());
                        eBiller.setDescription(name);
                        eBiller.setIsRequiredIdVerification(Boolean.TRUE);
                        eBiller.setServiceProviderId(serviceProviderId);
                        serviceBillerElectricAndBettings.add(eBiller);
                    }
                    serviceProviderBillerList.addAll(serviceBillerElectricAndBettings);
                    serviceProviderBillerRepository.saveAll(serviceBillerElectricAndBettings);

                }else if(category.getType().equalsIgnoreCase(BillCategoryName.vehicle.name()) ||
                        category.getType().equalsIgnoreCase(BillCategoryName.insurance.name())){

                    ServiceBillerLogoResponse serviceBillerResponse;
                    try {
                        ServiceCategoryName serviceCategoryName = new ServiceCategoryName();
                        serviceCategoryName.setService_type(category.getType());
                        serviceBillerResponse  = baxiProxy.fetchServiceProviderLogoByCategoryName(baxiApiKey,serviceCategoryName);
                        log.info("::ServiceBillerLogoResponse {0}, {1}",category.getType(), serviceBillerResponse);
                    }catch (FeignException ex){
                        log.error("::Error billerProvidersElect List {}",ex.getLocalizedMessage());
                        String msg = ex.contentUTF8();
                        int status = ex.status();
                        log.error(":: Message {0}, {1}",msg,status);
                        continue;
                    }

                    if(serviceBillerResponse.getData().size() > 0){
                        List<ServiceProviderBiller> billerList = new ArrayList<>();
                        for (ServiceBillerLogoDto billerDetail: serviceBillerResponse.getData()){

                            Optional<ServiceProviderBiller> betting = serviceProviderBillerRepository
                                    .findByNameAndServiceProviderCategoryAndIsActiveAndIsDeleted(billerDetail.getServiceName(),category,true,false);
                            if(betting.isPresent())
                                continue;

                            ServiceProviderBiller eBiller = new ServiceProviderBiller();
                            eBiller.setServiceProviderCategory(category);
                            eBiller.setCreatedBy(response.getData().getEmail());
                            eBiller.setModifiedBy(response.getData().getEmail());
                            eBiller.setProductId(String.valueOf(billerDetail.getBiller_id()));
                            eBiller.setBillerId(String.valueOf(billerDetail.getBiller_id()));
                            eBiller.setName(billerDetail.getServiceName());
                            eBiller.setType(billerDetail.getServiceType());
                            eBiller.setDescription(billerDetail.getServiceName());
                            eBiller.setImageLogo(billerDetail.getServiceLogo());
                            eBiller.setIsRequiredIdVerification(Boolean.TRUE);
                            eBiller.setServiceProviderId(serviceProviderId);
                            eBiller.setHasProduct(Boolean.TRUE);
                            billerList.add(eBiller);
                        }
                        serviceProviderBillerList.addAll(billerList);
                        serviceProviderBillerRepository.saveAll(billerList);
                    }
                }else {

                    GetAllBillersByCategoryResponse billerProviders;
                    try {
                        billerProviders  = baxiProxy.getAllBillersByCategory(baxiApiKey,category.getType());
                        log.info("::BillerProviders {0}, {1}",category.getType(),billerProviders);
                    }catch (FeignException ex){
                        log.error("::Error billerProviders List {}",ex.getLocalizedMessage());
                        String msg = ex.contentUTF8();
                        int status = ex.status();
                        log.error(":: Message {0}, {1}",msg,status);
                        continue;
                    }
                    List<ServiceProviderBiller> addServiceBillerList = new ArrayList<>();
                    for (BillerDetail billerDetail: billerProviders.getData().getProviders()){

                        Optional<ServiceProviderBiller> existingBiller = serviceProviderBillerRepository
                                .findByNameAndServiceProviderCategoryAndIsActiveAndIsDeleted(billerDetail.getName(),category,true,false);
                        if(existingBiller.isPresent())
                            continue;

                        ServiceProviderBiller biller = new ServiceProviderBiller();
                        biller.setServiceProviderCategory(category);
                        biller.setCreatedBy(response.getData().getEmail());
                        biller.setModifiedBy(response.getData().getEmail());
                        biller.setBillerId(billerDetail.getBiller_id());
                        biller.setProductId(billerDetail.getProduct_id());
                        biller.setName(billerDetail.getName());
                        biller.setType(billerDetail.getService_type());
                        biller.setDescription(billerDetail.getShortname());
                        biller.setServiceProviderId(serviceProviderId);
                        if(category.getType().equalsIgnoreCase(BillCategoryName.epin.name()) ||
                                category.getType().equalsIgnoreCase(BillCategoryName.databundle.name()) ||
                                category.getType().equalsIgnoreCase(BillCategoryName.cabletv.name())){
                            biller.setHasProduct(Boolean.TRUE);
                        }else {
                            biller.setHasProduct(Boolean.FALSE);
                        }
                        biller.setServiceProviderId(serviceProviderId);
                        if(category.getType().equalsIgnoreCase("airtime")){
                            if(billerDetail.getPlans() != null && billerDetail.getPlans().size() > 0){
                                String prepaid = billerDetail.getPlans().get(0);
                                if(prepaid.equalsIgnoreCase("prepaid")){
                                    biller.setPrepaidName(prepaid);
                                    biller.setIsPrepaid(true);
                                }
                                if(billerDetail.getPlans().size() > 1){
                                    String postpaid = billerDetail.getPlans().get(1);
                                    biller.setPostpaidName(postpaid);
                                    biller.setIsPostpaid(true);
                                }
                            }
                        }
                        addServiceBillerList.add(biller);
                    }
                    serviceProviderBillerList.addAll(addServiceBillerList);
                    serviceProviderBillerRepository.saveAll(addServiceBillerList);
                }
            }
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Success, Baxi service biller created",serviceProviderBillerList);
        }catch (Exception ex){
            log.error("::Error createBaxiServiceProviderBiller {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }


    @Override @Transactional
    public ApiResponse<?> createBaxiServiceProviderProduct(String token, Long serviceProviderId,Long serviceProviderCategoryId) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Baxi provider not found",null);

            Optional<ServiceProviderCategory> serviceProviderCategory = serviceProviderCategoryRepository.findByIdAndServiceProviderAndIsActiveAndIsDeleted(serviceProviderCategoryId,serviceProvider.get(),true,false);
            if(!serviceProviderCategory.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Baxi service category not found, please initiate service category before calling this service",null);

            List<ServiceProviderBiller> serviceProviderBillerList = serviceProviderBillerRepository.findAllByServiceProviderIdAndServiceProviderCategoryAndIsActiveAndIsDeleted(serviceProviderId,serviceProviderCategory.get(),true,false);
            if(serviceProviderBillerList.size() < 1)
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Baxi biller not found, please initiate service category before calling this service",null);


            //Todo: loop through each
            for (ServiceProviderBiller biller: serviceProviderBillerList){

                if(biller.getHasProduct().equals(Boolean.TRUE) &&
                        biller.getServiceProviderCategory().getType().equalsIgnoreCase(BillCategoryName.cabletv.name())){
                    //Todo: add product
                    CableTvPlanResponse cableTvPlanResponse;
                    try {
                        CableTvRequest cableTvRequest = new CableTvRequest();
                        cableTvRequest.setServiceType(biller.getType());
                        cableTvPlanResponse = baxiProxy.fetchCableTvProductAndBundle(baxiApiKey,cableTvRequest);
                        log.info("::cableTvPlanResponse {}",cableTvPlanResponse);
                        if(!cableTvPlanResponse.getCode().equals("200") && cableTvPlanResponse.getData().size() < 1)
                            continue;

                    }catch (FeignException ex){
                        log.error("::Error cableTvPlanResponse {}",ex.getLocalizedMessage());
                        String msg = ex.contentUTF8();
                        int status = ex.status();
                        log.error(":: Message {0}, {1}",msg,status);
                        continue;
                    }
                    //Todo: loop through each cable tv plans and add product
                    for(Plan plan: cableTvPlanResponse.getData()){
                        Optional<ServiceProviderProduct> providerProduct = serviceProviderProductRepository.findByNameAndProductCodeAndServiceProviderBillerAndIsActiveAndIsDeleted(plan.getName(),plan.getCode(),biller,true,false);
                        if(!providerProduct.isPresent()){
                            providerProduct = Optional.of(new ServiceProviderProduct());
                            providerProduct.get().setCreatedBy(response.getData().getEmail());
                            providerProduct.get().setModifiedBy(response.getData().getEmail());
                            providerProduct.get().setServiceProviderBiller(biller);
                        }
                        providerProduct.get().setHasBundle(true);
                        providerProduct.get().setHasTokenValidation(true);
                        providerProduct.get().setProductCode(plan.getCode());
                        providerProduct.get().setName(plan.getName());
                        if(plan.getDescription() != null)
                            providerProduct.get().setDescription(plan.getDescription());
                        providerProduct.get().setType(biller.getType());
                        serviceProviderProductRepository.save(providerProduct.get());
                        //Todo: loop through bundle to add bundle package
                        createAndUpdateProduct( plan,  response,  providerProduct);

                        //Todo: check if this product has ADDONS
                        CableTvAddonsResponse cableTvAddonsResponse;
                        try {
                            AddonRequest addonRequest = new AddonRequest();
                            addonRequest.setServiceType(biller.getType());
                            addonRequest.setProductCode(providerProduct.get().getProductCode());
                            cableTvAddonsResponse = baxiProxy.getCableTvAddons(baxiApiKey,addonRequest);
                            log.info("::cableTvAddonsResponse {}",cableTvPlanResponse);
                            if(!cableTvPlanResponse.getCode().equals("200") && cableTvPlanResponse.getData().size() < 1)
                                continue;
                        }catch (FeignException ex){
                            log.error("::Error cableTvAddonsResponse {}",ex.getLocalizedMessage());
                            String msg = ex.contentUTF8();
                            int status = ex.status();
                            log.error(":: Message {0}, {1}",msg,status);
                            continue;
                        }
                        createAndUpdateAddOns( response,  providerProduct, cableTvAddonsResponse);
                    }

                } else if(biller.getHasProduct().equals(Boolean.TRUE) &&
                        biller.getServiceProviderCategory().getType().equalsIgnoreCase(BillCategoryName.databundle.name())){

                    DataBundleResponse dataBundleResponse;
                    try {
                        DataBundleRequest dataBundleRequest = new DataBundleRequest();
                        dataBundleRequest.setServiceType(biller.getType());
                        dataBundleRequest.setAccountNumber(null);
                        dataBundleResponse = baxiProxy.getDataBundles(baxiApiKey,dataBundleRequest);
                        log.info("::dataBundleResponse {}",dataBundleResponse);
                        if(!dataBundleResponse.getCode().equals("200") && dataBundleResponse.getData().size() < 1)
                            continue;
                    }catch (FeignException ex){
                        log.error("::Error dataBundleResponse {}",ex.getLocalizedMessage());
                        String msg = ex.contentUTF8();
                        int status = ex.status();
                        log.error(":: Message {0}, {1}",msg,status);
                        continue;
                    }
                    //Todo: loop through each data bundle
                    createAndUpdateDataBundle( dataBundleResponse, response, biller);

                } else if(biller.getHasProduct().equals(Boolean.TRUE) &&
                        biller.getServiceProviderCategory().getType().equalsIgnoreCase(BillCategoryName.epin.name())){

                    EPinBundleResponse ePinBundleResponse;
                    try {
                        EpinBundleDto epinBundleDto = new EpinBundleDto();
                        epinBundleDto.setService_type(biller.getType());
                        epinBundleDto.setAccount_number(null);
                        ePinBundleResponse = baxiProxy.getEpinBundles(baxiApiKey,epinBundleDto);
                        log.info("::ePinBundleResponse {}",ePinBundleResponse);
                        if(!ePinBundleResponse.getCode().equals("200") && ePinBundleResponse.getData().size() < 1)
                            continue;
                    }catch (FeignException ex){
                        log.error("::Error ePinBundleResponse {}",ex.getLocalizedMessage());
                        String msg = ex.contentUTF8();
                        int status = ex.status();
                        log.error(":: Message {0}, {1}",msg,status);
                        continue;
                    }
                    //Todo: loop through each epin bundle
                    createAndUpdateEpinBundle( ePinBundleResponse, response, biller);

                }else {
                   continue;
                }
            }
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Baxi biller product created successfully",null);
        }catch (Exception ex){
            log.error("::Error createBaxiServiceProviderProduct {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }


    @Override @Transactional
    public ApiResponse<?> createBaxiServiceProviderBillerLogo(String token, Long serviceProviderId,Long serviceProviderCategoryId) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Baxi provider not found",null);

            Optional<ServiceProviderCategory> category = serviceProviderCategoryRepository.findByIdAndServiceProviderAndIsActiveAndIsDeleted(serviceProviderCategoryId,serviceProvider.get(),true, false);
            if(!category.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Baxi provider category not found",null);

            List<ServiceProviderBiller> providerBillerList  = serviceProviderBillerRepository.findAllByServiceProviderCategoryAndIsActiveAndIsDeleted(category.get(),true,false);
            if(providerBillerList.size() < 1)
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"No Baxi biller available for Logo update",null);

            ServiceBillerLogoResponse billerLogoResponse;
            try {
                ServiceCategoryName serviceCategoryName = new ServiceCategoryName();
                serviceCategoryName.setService_type(category.get().getType());

                billerLogoResponse = baxiProxy.fetchServiceProviderLogoByCategoryName(baxiApiKey,serviceCategoryName);
                log.info("::billerLogoResponse {0}, {1}",category.get().getType(),billerLogoResponse);
                if(!billerLogoResponse.getCode().equals("200") && billerLogoResponse.getData().size() < 1)
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,billerLogoResponse.getMessage(),null);

            }catch (FeignException ex){
                log.error("::Error ePinBundleResponse {}",ex.getLocalizedMessage());
                String msg = ex.contentUTF8();
                int status = ex.status();
                log.error(":: Message {0}, {1}",msg,status);
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,msg,null);
            }
            //Todo: processing biller category logo
            List<ServiceProviderBiller> serviceProviderBillerList = new ArrayList<>();
            for (ServiceBillerLogoDto logo: billerLogoResponse.getData()){
                try {
                    if(!category.get().getType().equalsIgnoreCase(BillCategoryName.electricity.name())){

                        //Todo: we are processing this airtime , databundle , cabletv , epin
                        Optional<ServiceProviderBiller> providerBiller = serviceProviderBillerRepository.findByTypeAndServiceProviderCategoryAndIsActiveAndIsDeleted(logo.getServiceType(),category.get(),true,false);
                        if(!providerBiller.isPresent())
                            continue;

                        providerBiller.get().setImageLogo(logo.getServiceLogo());
                        providerBiller.get().setModifiedAt(LocalDateTime.now());
                        providerBiller.get().setModifiedBy(response.getData().getEmail());
                        serviceProviderBillerList.add(providerBiller.get());
                    }else {
                        Optional<ServiceProviderBiller> providerBiller = serviceProviderBillerRepository.findByPrepaidNameOrPostpaidNameAndServiceProviderCategoryAndIsActiveAndIsDeleted(logo.getServiceType(),logo.getServiceType(),category.get(),true,false);
                        if(!providerBiller.isPresent())
                            continue;

                        providerBiller.get().setImageLogo(logo.getServiceLogo());
                        providerBiller.get().setModifiedAt(LocalDateTime.now());
                        providerBiller.get().setModifiedBy(response.getData().getEmail());
                        serviceProviderBillerList.add(providerBiller.get());
                    }
                }catch (Exception ex){
                    log.error("::Error Baxi LogoService {}",ex.getLocalizedMessage());
                    ex.printStackTrace();
                    continue;
                }
            }
            if(serviceProviderBillerList.size() > 0)
                serviceProviderBillerRepository.saveAll(serviceProviderBillerList);

            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Billers Logo successfully updated",null);
        }catch (Exception ex){
            log.error("::Error createBaxiServiceProviderBillerLogo {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }


    @Override @Transactional
    public ApiResponse<?> verifyCustomerAccountNumberOrSmartCardOrMeterNumber(String type, String account, String categoryType) {
        try {
            CustomerTokenValidationDto tokenValidationDto = new CustomerTokenValidationDto();
            if(categoryType.equalsIgnoreCase(BillCategoryName.electricity.name())){
                ElectricityRequest request = new ElectricityRequest();
                request.setService_type(type);
                request.setAccount_number(account);
                ElectricityVerificationResponse verificationResponse;
                try {
                    verificationResponse = baxiProxy.verifyCustomerElectricityDetail(baxiApiKey,request);
                    if(!verificationResponse.getCode().equals("200") && verificationResponse.getData() == null)
                        return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,verificationResponse.getMessage(),null);
                }catch (FeignException ex){
                    log.error("::Error Baxi Verify {}",ex.getLocalizedMessage());
                    String msg = ex.contentUTF8();
                    int status =  ex.status();
                    if(msg.contains("message")){
                        String error = getErrorMessage(msg);
                        return new ApiResponse<>(false,HttpStatus.valueOf(status).value(),error,null);
                    }
                    return new ApiResponse<>(false,HttpStatus.valueOf(status).value(),msg,null);
                }
                tokenValidationDto.setAccountNumber(verificationResponse.getData().getUser().getAccountNumber());
                tokenValidationDto.setName(verificationResponse.getData().getUser().getName());
            }else {
                NameFinderQueryResponse verificationResponse;
                try {
                    NameQueryDto nameQueryDto = new NameQueryDto();
                    nameQueryDto.setService_type(type);
                    nameQueryDto.setAccount_number(account);
                    verificationResponse = baxiProxy.nameFinderEnquiry(baxiApiKey,nameQueryDto);
                    if(!verificationResponse.getCode().equals("200") && verificationResponse.getData() == null)
                        return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,verificationResponse.getMessage(),null);
                }catch (FeignException ex){
                    log.error("::Error Baxi Verify {}",ex.getLocalizedMessage());
                    String msg = ex.contentUTF8();
                    int status =  ex.status();
                    if(msg.contains("message")){
                        String error = getErrorMessage(msg);
                        return new ApiResponse<>(false,HttpStatus.valueOf(status).value(),error,null);
                    }
                    return new ApiResponse<>(false,HttpStatus.valueOf(status).value(),msg,null);
                }
                tokenValidationDto.setAccountNumber(verificationResponse.getData().getUser().getAccountNumber());
                tokenValidationDto.setName(verificationResponse.getData().getUser().getName());
            }
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Success",tokenValidationDto);
        }catch (Exception ex){
            log.error("::Error verifyCustomerAccountNumberOrSmartCardOrMeterNumber {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to validate your token id, can not process your request",null);
        }
    }


    @Override
    public ApiResponse<?> requestElectricityPayment(BigDecimal amount, String type,String phone,String account,String reference) {
        try {
            ElectricPaymentResponse paymentResponse;
            try {
                ElectricPaymentRequest paymentRequest = new ElectricPaymentRequest();
                paymentRequest.setAmount(amount);
                paymentRequest.setService_type(type);
                paymentRequest.setAgentId(baxiAgentCode);
                paymentRequest.setMetadata("");
                paymentRequest.setPhone(phone);
                paymentRequest.setAccount_number(account);
                paymentRequest.setAgentReference(reference);
                paymentResponse = baxiProxy.electricityPayment(baxiApiKey,paymentRequest);
                log.info(":::Electric paymentResponse {}",paymentResponse);
                if(!paymentResponse.getCode().equals("200") && paymentResponse.getData() == null)
                    return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,paymentResponse.getMessage(),null);

            }catch (FeignException ex){
                log.error("::Error Electricity paymentResponse {}",ex.getLocalizedMessage());
                ex.printStackTrace();
                String msg = ex.contentUTF8();
                int status = ex.status();
                if(msg.contains("message")){
                    String error = getErrorMessage(msg);
                    return new ApiResponse<>(false,HttpStatus.valueOf(status).value(),error,null);
                }
                return new ApiResponse<>(false,HttpStatus.valueOf(status).value(),msg,null);
            }

            GeneralPaymentResponseDto responseDto = new GeneralPaymentResponseDto();
            BeanUtils.copyProperties(paymentResponse.getData(),responseDto);
            responseDto.setStatusCode(paymentResponse.getData().getStatusCode());
            responseDto.setStatusMessage(paymentResponse.getData().getTransactionStatus());
            responseDto.setProviderReference(paymentResponse.getData().getBaxiReference());
            responseDto.setProviderMessage(paymentResponse.getData().getProvider_message());

            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Payment successful",responseDto);
        }catch (Exception ex){
            log.error("::Error requestElectricityPayment {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process electricity payment, try again later",null);
        }
    }

    @Override
    public ApiResponse<?> requestDataBundlePayment(String productCode,String type,String amount,String phone,String reference) {
        try {
            BundlePaymentResponse bundlePaymentResponse;
            try {
                DataPayment paymentRequest = new DataPayment();
                paymentRequest.setAmount(amount);
                paymentRequest.setServiceType(type);
                paymentRequest.setAgentId(baxiAgentCode);
                paymentRequest.setDatacode(productCode);
                paymentRequest.setPhone(phone);
                paymentRequest.setAgentReference(reference);
                bundlePaymentResponse = baxiProxy.bundlePayment(baxiApiKey,paymentRequest);
                log.info(":::bundlePaymentResponse {}",bundlePaymentResponse);
                if(!bundlePaymentResponse.getCode().equals("200") && bundlePaymentResponse.getData() == null)
                    return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,bundlePaymentResponse.getMessage(),null);

            }catch (FeignException ex){
                log.error("::Error requestDataBundlePayment {}",ex.getLocalizedMessage());
                ex.printStackTrace();
                String msg = ex.contentUTF8();
                int status = ex.status();
                if(msg.contains("message")){
                    String error = getErrorMessage(msg);
                    return new ApiResponse<>(false,HttpStatus.valueOf(status).value(),error,null);
                }
                return new ApiResponse<>(false,HttpStatus.valueOf(status).value(),msg,null);
            }

            GeneralPaymentResponseDto responseDto = new GeneralPaymentResponseDto();
            responseDto.setStatusCode(bundlePaymentResponse.getData().getStatusCode());
            responseDto.setStatusMessage(bundlePaymentResponse.getData().getTransactionStatus());
            responseDto.setTransactionMessage(bundlePaymentResponse.getData().getTransactionMessage());
            responseDto.setTransactionReference(bundlePaymentResponse.getData().getTransactionReference());
            responseDto.setProviderReference(bundlePaymentResponse.getData().getBaxiReference());
            responseDto.setProviderMessage(bundlePaymentResponse.getData().getProvider_message());
            if(bundlePaymentResponse.getData().getExtraData() != null){
                responseDto.setCaptureUrl(bundlePaymentResponse.getData().getExtraData().getCaptureUrl());
                responseDto.setPurchasedDuration(bundlePaymentResponse.getData().getExtraData().getPurchasedDuration());
                responseDto.setVoucherCode(bundlePaymentResponse.getData().getExtraData().getVoucherCode());
                responseDto.setPurchasedPackage(bundlePaymentResponse.getData().getExtraData().getPurchasedPackage());
            }
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Payment successful",responseDto);
        }catch (Exception ex){
            log.error("::Error requestDataBundlePayment {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process data bundle payment, try again later",null);
        }
    }



    @Override
    public ApiResponse<?> requestAirtimePayment(int amount, String plan,String phone,String reference,String type) {
        try {
            AirtimePaymentResponse paymentResponse;
            try {
                AirtimePayment paymentRequest = new AirtimePayment();
                paymentRequest.setAmount(amount);
                paymentRequest.setServiceType(type);
                paymentRequest.setAgentId(baxiAgentCode);
                paymentRequest.setPhone(phone);
                paymentRequest.setPlan(plan);
                paymentRequest.setAgentReference(reference);
                paymentResponse = baxiProxy.airtimePayment(baxiApiKey,paymentRequest);
                log.info(":::Airtime paymentResponse {}",paymentResponse);
                if(!paymentResponse.getCode().equals("200") && paymentResponse.getData() == null)
                    return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,paymentResponse.getMessage(),null);

            }catch (FeignException ex){
                log.error("::Airtime paymentResponse {}",ex.getLocalizedMessage());
                ex.printStackTrace();
                String msg = ex.contentUTF8();
                int status = ex.status();
                if(msg.contains("message")){
                    String error = getErrorMessage(msg);
                    return new ApiResponse<>(false,HttpStatus.valueOf(status).value(),error,null);
                }
                return new ApiResponse<>(false,HttpStatus.valueOf(status).value(),msg,null);
            }

            GeneralPaymentResponseDto responseDto = new GeneralPaymentResponseDto();
            BeanUtils.copyProperties(paymentResponse.getData(),responseDto);
            responseDto.setStatusCode(paymentResponse.getData().getStatusCode());
            responseDto.setStatusMessage(paymentResponse.getData().getTransactionStatus());
            responseDto.setProviderReference(paymentResponse.getData().getBaxiReference());
            responseDto.setProviderMessage(paymentResponse.getData().getProvider_message());

            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Payment successful",responseDto);
        }catch (Exception ex){
            log.error("::Error requestAirtimePayment {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process airtime payment, try again later",null);
        }
    }



    @Override
    public ApiResponse<?> requestEpinPayment(int numberOfPins, int amount, int fixAmount, String type,String reference) {
        try {
            EPinPaymentResponse paymentResponse;
            try {
                EPinPaymentRequest paymentRequest = new EPinPaymentRequest();
                paymentRequest.setAmount(amount);
                paymentRequest.setServiceType(type);
                paymentRequest.setAgentId(baxiAgentCode);
                paymentRequest.setNumberOfPins(numberOfPins);
                paymentRequest.setPinValue(fixAmount);
                paymentRequest.setAgentReference(reference);
                paymentResponse = baxiProxy.epinPayment(baxiApiKey,paymentRequest);
                log.info(":::Epin paymentResponse {}",paymentResponse);
                if(!paymentResponse.getCode().equals("200") && paymentResponse.getData() == null)
                    return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,paymentResponse.getMessage(),null);

            }catch (FeignException ex){
                log.error("::Epin requestEpinPayment {}",ex.getLocalizedMessage());
                ex.printStackTrace();
                String msg = ex.contentUTF8();
                int status = ex.status();
                if(msg.contains("message")){
                    String error = getErrorMessage(msg);
                    return new ApiResponse<>(false,HttpStatus.valueOf(status).value(),error,null);
                }
                return new ApiResponse<>(false,HttpStatus.valueOf(status).value(),msg,null);
            }

            GeneralPaymentResponseDto responseDto = new GeneralPaymentResponseDto();
            BeanUtils.copyProperties(paymentResponse.getData(),responseDto);
            responseDto.setStatusCode(paymentResponse.getData().getStatusCode());
            responseDto.setStatusMessage(paymentResponse.getData().getTransactionStatus());
            responseDto.setProviderReference(paymentResponse.getData().getBaxiReference());
            responseDto.setProviderMessage(paymentResponse.getData().getProvider_message());
            if(paymentResponse.getData().getPins().size() > 0){
                List<GeneralEpinData> epinDataList = new ArrayList<>();
                for (PinDetail pin: paymentResponse.getData().getPins()){
                    GeneralEpinData data = new GeneralEpinData();
                    BeanUtils.copyProperties(pin,data);
                    epinDataList.add(data);
                }
                responseDto.setPins(epinDataList);
            }
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Payment successful",responseDto);
        }catch (Exception ex){
            log.error("::Error requestEpinPayment {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process epin payment, try again later",null);
        }
    }

    @Override
    public ApiResponse<?> requestCableTvPayment(String type,String phone,String amount,String reference,String productCode,String smartCardNumber,String monthPaidFor) {
        try {
            CablePaymentResponse paymentResponse;
            try {
                CablePaymentRequest paymentRequest = new CablePaymentRequest();
                paymentRequest.setProductMonthsPaidFor(monthPaidFor);
                paymentRequest.setSmartCardNumber(smartCardNumber);
                paymentRequest.setProductCode(productCode);
                paymentRequest.setServiceType(type);
                paymentRequest.setAgentId(baxiAgentCode);
                paymentRequest.setPhone(phone);
                paymentRequest.setTotalAmount(Integer.parseInt(amount));
                paymentRequest.setAgentReference(reference);
                paymentResponse = baxiProxy.cableTvPayment(baxiApiKey,paymentRequest);
                log.info(":::CableTv paymentResponse {}",paymentResponse);
                if(!paymentResponse.getCode().equals("200") && paymentResponse.getData() == null)
                    return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,paymentResponse.getMessage(),null);

            }catch (FeignException ex){
                log.error("::CableTv requestCableTvPayment {}",ex.getLocalizedMessage());
                ex.printStackTrace();
                String msg = ex.contentUTF8();
                int status = ex.status();
                if(msg.contains("message")){
                    String error = getErrorMessage(msg);
                    return new ApiResponse<>(false,HttpStatus.valueOf(status).value(),error,null);
                }
                return new ApiResponse<>(false,HttpStatus.valueOf(status).value(),msg,null);
            }

            GeneralPaymentResponseDto responseDto = new GeneralPaymentResponseDto();
            BeanUtils.copyProperties(paymentResponse.getData(),responseDto);
            responseDto.setStatusCode(paymentResponse.getData().getStatusCode());
            responseDto.setStatusMessage(paymentResponse.getData().getTransactionStatus());
            responseDto.setProviderReference(paymentResponse.getData().getBaxiReference());
            responseDto.setProviderMessage(String.valueOf(paymentResponse.getData().getProvider_message()));
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Payment successful",responseDto);
        }catch (Exception ex){
            log.error("::Error requestCableTvPayment {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process cable tv payment, try again later",null);
        }
    }


    @Override
    public ApiResponse<?> requestBettingPayment(BigDecimal amount, String type,String reference,String accountNumber) {
        try {
            BettingPaymentRespose paymentResponse;
            try {
                BettingRequest paymentRequest = new BettingRequest();
                paymentRequest.setAction("WALLET_FUNDING");
                paymentRequest.setAccountNumber(accountNumber);
                paymentRequest.setServiceType(type);
                paymentRequest.setAmount(String.valueOf(amount));
                paymentRequest.setAgentReference(reference);
                paymentResponse = baxiProxy.bettingPayment(baxiApiKey,paymentRequest);
                log.info(":::Betting paymentResponse {}",paymentResponse);
                if(!paymentResponse.getCode().equals("200") && paymentResponse.getData() == null)
                    return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,paymentResponse.getMessage(),null);

            }catch (FeignException ex){
                log.error("::Betting requestBettingPayment {}",ex.getLocalizedMessage());
                ex.printStackTrace();
                String msg = ex.contentUTF8();
                int status = ex.status();
                if(msg.contains("message")){
                    String error = getErrorMessage(msg);
                    return new ApiResponse<>(false,HttpStatus.valueOf(status).value(),error,null);
                }
                return new ApiResponse<>(false,HttpStatus.valueOf(status).value(),msg,null);
            }

            GeneralPaymentResponseDto responseDto = new GeneralPaymentResponseDto();
            BeanUtils.copyProperties(paymentResponse.getData(),responseDto);
            responseDto.setStatusCode(paymentResponse.getData().getStatusCode());
            responseDto.setStatusMessage(paymentResponse.getData().getTransactionStatus());
            responseDto.setProviderReference(paymentResponse.getData().getBaxiReference());
            responseDto.setProviderMessage(String.valueOf(paymentResponse.getData().getProvider_message()));
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Payment successful",responseDto);
        }catch (Exception ex){
            log.error("::Error requestBettingPayment {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to process game betting payment, try again later",null);
        }
    }

    @Override
    public ApiResponse<?> fetchTransactionQuery(String token, String reference) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Object queryTxn = baxiProxy.reQueryTransaction(baxiApiKey,reference);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Transaction fetched",queryTxn);
        }catch (Exception ex){
            log.error("::Error fetchTransactionQuery {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Unable to fetch transaction query, try again later",null);
        }
    }


    private void createAndUpdateDataBundle(DataBundleResponse dataBundleResponse,AuthResponse response,ServiceProviderBiller biller){
        try {
            for(DataBundle bundle: dataBundleResponse.getData()){
                try {
                    Optional<ServiceProviderProduct> providerProduct = serviceProviderProductRepository.findByNameAndProductCodeAndServiceProviderBillerAndIsActiveAndIsDeleted(bundle.getName(),bundle.getDatacode(),biller,true,false);
                    if(!providerProduct.isPresent()){
                        providerProduct = Optional.of(new ServiceProviderProduct());
                        providerProduct.get().setCreatedBy(response.getData().getEmail());
                        providerProduct.get().setModifiedBy(response.getData().getEmail());
                        providerProduct.get().setServiceProviderBiller(biller);
                    }
                    providerProduct.get().setHasBundle(true);
                    providerProduct.get().setHasTokenValidation(false);
                    providerProduct.get().setProductCode(bundle.getDatacode());
                    providerProduct.get().setName(bundle.getName());
                    providerProduct.get().setDescription(bundle.getName());
                    providerProduct.get().setType(biller.getType());
                    serviceProviderProductRepository.save(providerProduct.get());
                    Optional<ServiceProviderProductBundle> providerProductBundle = serviceProviderProductBundleRepository.findByNameAndBundleCodeAndServiceProviderProductAndIsActiveAndIsDeleted(bundle.getName(), bundle.getDatacode(), providerProduct.get(),true,false);
                    if(!providerProductBundle.isPresent()){
                        providerProductBundle = Optional.of(new ServiceProviderProductBundle());
                        providerProductBundle.get().setCreatedBy(response.getData().getEmail());
                        providerProductBundle.get().setModifiedBy(response.getData().getEmail());
                        providerProductBundle.get().setName(bundle.getName());
                        providerProductBundle.get().setAllowance(bundle.getAllowance());
                        providerProductBundle.get().setAmount(new BigDecimal(bundle.getPrice()));
                    }else {
                        providerProductBundle.get().setName(bundle.getName());
                        providerProductBundle.get().setAllowance(bundle.getAllowance());
                        providerProductBundle.get().setAmount(new BigDecimal(bundle.getPrice()));
                        providerProductBundle.get().setValidity(bundle.getValidity());
                    }
                    providerProductBundle.get().setServiceProviderProduct(providerProduct.get());
                    providerProductBundle.get().setHasAddOns(Boolean.FALSE);
                    providerProductBundle.get().setBundleCode(bundle.getDatacode());
                    providerProductBundle.get().setMonthsPaidFor(bundle.getValidity());
                    providerProductBundle.get().setInvoicePeriod(bundle.getAllowance());
                    providerProductBundle.get().setDescription(bundle.getName());
                    serviceProviderProductBundleRepository.save(providerProductBundle.get());
                }catch (Exception ex){
                    log.error("::Error Loop createAndUpdateDataBundle {}",ex.getLocalizedMessage());
                    ex.printStackTrace();
                    continue;
                }
            }
        }catch (Exception ex){
            log.error("::Error ",ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    private void createAndUpdateEpinBundle(EPinBundleResponse ePinBundleResponse,AuthResponse response,ServiceProviderBiller biller){
        try {
            for(EPinBundle bundle: ePinBundleResponse.getData()){
                try {
                    Optional<ServiceProviderProduct> providerProduct = serviceProviderProductRepository.findByNameAndTypeAndServiceProviderBillerAndIsActiveAndIsDeleted(biller.getName(),biller.getType(),biller,true,false);
                    if(!providerProduct.isPresent()){
                        providerProduct = Optional.of(new ServiceProviderProduct());
                        providerProduct.get().setCreatedBy(response.getData().getEmail());
                        providerProduct.get().setModifiedBy(response.getData().getEmail());
                        providerProduct.get().setServiceProviderBiller(biller);
                    }
                    providerProduct.get().setHasBundle(true);
                    providerProduct.get().setHasTokenValidation(false);
                    providerProduct.get().setName(biller.getName());
                    providerProduct.get().setDescription(bundle.getDescription());
                    providerProduct.get().setProductCode(bundle.getAmount());
                    providerProduct.get().setType(biller.getType());
                    serviceProviderProductRepository.save(providerProduct.get());
                    Optional<ServiceProviderProductBundle> providerProductBundle = serviceProviderProductBundleRepository.findByNameAndServiceProviderProductAndIsActiveAndIsDeleted(providerProduct.get().getName(), providerProduct.get(),true,false);
                    if(!providerProductBundle.isPresent()){
                        providerProductBundle = Optional.of(new ServiceProviderProductBundle());
                        providerProductBundle.get().setCreatedBy(response.getData().getEmail());
                        providerProductBundle.get().setModifiedBy(response.getData().getEmail());
                    }
                    providerProductBundle.get().setName(providerProduct.get().getName());
                    providerProductBundle.get().setAllowance(bundle.getAvailable());
                    providerProductBundle.get().setAmount(new BigDecimal(bundle.getAmount()));
                    providerProductBundle.get().setServiceProviderProduct(providerProduct.get());
                    providerProductBundle.get().setHasAddOns(Boolean.FALSE);
                    providerProductBundle.get().setBundleCode(bundle.getAmount());
                    providerProductBundle.get().setDescription(bundle.getDescription());
                    providerProductBundle.get().setEpinAvailable(Integer.parseInt(bundle.getAvailable()));
                    serviceProviderProductBundleRepository.save(providerProductBundle.get());
                }catch (Exception ex){
                    log.error("::Error Loop createAndUpdateEpinBundle {}",ex.getLocalizedMessage());
                    ex.printStackTrace();
                    continue;
                }
            }
        }catch (Exception ex){
            log.error("::Error createAndUpdateEpinBundle {}",ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }


    private void createAndUpdateAddOns( AuthResponse response, Optional<ServiceProviderProduct> providerProduct,CableTvAddonsResponse cableTvAddonsResponse){
        try {
            for (CableTvDetail plan: cableTvAddonsResponse.getData()){
                try {
                    List<ServiceProviderProductBundle> addonsProductBundleList = new ArrayList<>();
                    for(CableTvPlan pricing: plan.getAvailablePricingOptions()){
                        try {
                            BigDecimal amount = new BigDecimal(pricing.getPrice());
                            Optional<ServiceProviderProductBundle> productBundle = serviceProviderProductBundleRepository.findByNameAndBundleCodeAndMonthsPaidForAndServiceProviderProductAndIsActiveAndIsDeleted(plan.getName(), plan.getCode(), pricing.getMonthsPaidFor(), providerProduct.get(),true,false);
                            if(!productBundle.isPresent()){
                                productBundle  = Optional.of(new ServiceProviderProductBundle());
                                productBundle.get().setCreatedBy(response.getData().getEmail());
                                productBundle.get().setModifiedBy(response.getData().getEmail());
                                productBundle.get().setServiceProviderProduct(providerProduct.get());
                                productBundle.get().setBundleCode(plan.getCode());
                                productBundle.get().setName(plan.getName());
                                productBundle.get().setAmount(amount);
                            }
                            productBundle.get().setAmount(amount);
                            productBundle.get().setInvoicePeriod(pricing.getInvoicePeriod());
                            productBundle.get().setMonthsPaidFor(pricing.getMonthsPaidFor());
                            if(plan.getDescription() != null)
                                productBundle.get().setDescription(plan.getDescription());
                            if(!pricing.getMonthsPaidFor().equals("1")){
                                productBundle.get().setValidity(pricing.getMonthsPaidFor() + " Months");
                            }else {
                                productBundle.get().setValidity(pricing.getMonthsPaidFor() + " Month");
                            }
                            productBundle.get().setAllowance(pricing.getInvoicePeriod());
                            productBundle.get().setHasAddOns(Boolean.TRUE);

                            providerProduct.get().setHasAddOns(Boolean.TRUE);
                            providerProduct.get().setModifiedAt(LocalDateTime.now());
                            serviceProviderProductRepository.save(providerProduct.get());

                            addonsProductBundleList.add(productBundle.get());
                        }catch (Exception ex){
                            log.error("::Error Loop Addon bundle cableTvAddonsResponse {}",ex.getLocalizedMessage());
                            ex.printStackTrace();
                            continue;
                        }
                    }
                    serviceProviderProductBundleRepository.saveAll(addonsProductBundleList);
                }catch (Exception ex){
                    log.error("::Error Loop Addon product cableTvAddonsResponse {}",ex.getLocalizedMessage());
                    ex.printStackTrace();
                    continue;
                }
            }
        }catch (Exception ex){
            log.error("::Error createAndUpdateAddOns {}",ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    private void createAndUpdateProduct(Plan plan, AuthResponse response, Optional<ServiceProviderProduct> providerProduct){
        try {
            List<ServiceProviderProductBundle> productBundleList = new ArrayList<>();
            for(Pricing pricing: plan.getAvailablePricingOptions()){
                try {
                    BigDecimal amount = new BigDecimal(pricing.getPrice());
                    Optional<ServiceProviderProductBundle> productBundle = serviceProviderProductBundleRepository.findByNameAndBundleCodeAndMonthsPaidForAndServiceProviderProductAndIsActiveAndIsDeleted(plan.getName(), plan.getCode(), pricing.getMonthsPaidFor(), providerProduct.get(),true,false);
                    if(!productBundle.isPresent()){
                        productBundle  = Optional.of(new ServiceProviderProductBundle());
                        productBundle.get().setCreatedBy(response.getData().getEmail());
                        productBundle.get().setModifiedBy(response.getData().getEmail());
                        productBundle.get().setServiceProviderProduct(providerProduct.get());
                        productBundle.get().setBundleCode(providerProduct.get().getProductCode());
                        productBundle.get().setName(providerProduct.get().getName());
                        productBundle.get().setAmount(amount);
                    }else {
                        productBundle.get().setAmount(amount);
                    }
                    productBundle.get().setInvoicePeriod(pricing.getInvoicePeriod());
                    productBundle.get().setMonthsPaidFor(pricing.getMonthsPaidFor());
                    if(plan.getDescription() != null)
                        productBundle.get().setDescription(plan.getDescription());
                    if(!pricing.getMonthsPaidFor().equals("1")){
                        productBundle.get().setValidity(pricing.getMonthsPaidFor() + " Months");
                    }else {
                        productBundle.get().setValidity(pricing.getMonthsPaidFor() + " Month");
                    }
                    productBundle.get().setAllowance(pricing.getMonthsPaidFor());
                    productBundleList.add(productBundle.get());
                }catch (Exception ex){
                    log.error("::Error bundle createAndUpdateProduct {}",ex.getLocalizedMessage());
                    ex.printStackTrace();
                    continue;
                }
            }
            serviceProviderProductBundleRepository.saveAll(productBundleList);
        }catch (Exception ex){
            log.error("::Error createAndUpdateProduct {}",ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }



    private String getBillerServiceName(BillerDetail billerDetail, String serviceType){
        try {
            String name;
            if(serviceType.equalsIgnoreCase("electricity")){
                String serviceName = billerDetail.getName();
                if(serviceName.contains("prepaid")){
                    String replaceName = serviceName.replace("prepaid", "").trim();
                    String capitalizeName = WordUtils.capitalize(replaceName);
                    name = capitalizeName + " Electricity Distribution Plc";
                }else if(serviceName.contains("postpaid")){
                    String replaceName = serviceName.replace("postpaid", "").trim();
                    String capitalizeName = WordUtils.capitalize(replaceName);
                    name = capitalizeName + " Electricity Distribution Plc";
                }else {
                    name = serviceName;
                }
            }else {
                name = billerDetail.getName();
            }
            return name;
        }catch (Exception ex){
            log.error("::Error getBillerServiceName {}",ex.getLocalizedMessage());
            return null;
        }
    }


    public String getErrorMessage(String errorInJson) {
        try {
            return CommonUtils.getObjectMapper().readValue(errorInJson, BaxiErrorDto.class).getMessage();
        } catch (JsonProcessingException e) {
            log.error(":::Error getErrorMessage {}", e.getLocalizedMessage());
            return Constants.ERROR_MESSAGE;
        }
    }

    private String formatErrorMessage(String message){
        try {
            if(message.contains("message")){
                JsonNode jsonNode = CommonUtils.getObjectMapper().readTree(message);
                String msg = jsonNode.get("message").asText();
                return  msg;
            }
            return null;
        }catch (Exception ex){
            log.error("::Error formatErrorMessage {}",ex.getLocalizedMessage());
            return null;
        }
    }

}
