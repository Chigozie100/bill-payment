package com.wayapay.thirdpartyintegrationservice.v2.service.impl;

import com.wayapay.thirdpartyintegrationservice.v2.dto.PaymentStatus;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.AuthResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.BillCategoryName;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.*;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.CustomObject;
import com.wayapay.thirdpartyintegrationservice.v2.entity.*;
import com.wayapay.thirdpartyintegrationservice.v2.proxyclient.AuthProxy;
import com.wayapay.thirdpartyintegrationservice.v2.repository.*;
import com.wayapay.thirdpartyintegrationservice.v2.service.AdminBillPaymentService;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.BaxiProxy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service @Slf4j @RequiredArgsConstructor
public class AdminBillPaymentServiceImpl implements AdminBillPaymentService {

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
    private final AuthProxy authProxy;
    private final BaxiProxy baxiProxy;

    @Value("${app.config.baxi.x-api-key}")
    private String baxiApiKey;

    @Value("${app.config.baxi.agent-code}")
    private String baxiAgentCode;



    @Override
    public ApiResponse<?> createServiceProvider(String token, CategoryDto request) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> exist = serviceProviderRepository.findFirstByNameStartingWithIgnoreCase(request.getName());
            if(exist.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Bill service provider already exist, you can choose to activate it",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByNameAndIsDeleted(request.getName(), false);
            if(serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Bill service provider already exist, you can choose to activate it",null);

            ServiceProvider provider = new ServiceProvider();
            provider.setModifiedBy(response.getData().getEmail());
            provider.setCreatedBy(response.getData().getEmail());
            BeanUtils.copyProperties(request,provider);
            provider.setName(request.getName());
            serviceProviderRepository.save(provider);

            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Bill provider created",provider);
        }catch (Exception ex){
            log.error("::Error createServiceProvider {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }


    @Override
    public ApiResponse<?> updateServiceProvider(String token, UpdateServiceProvider request, Long serviceProviderId) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true, false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Bill service provider not found",null);

            ServiceProvider provider = serviceProvider.get();
            provider.setModifiedAt(LocalDateTime.now());
            provider.setModifiedBy(response.getData().getEmail());
            BeanUtils.copyProperties(request,provider);
            serviceProviderRepository.save(provider);

            return new ApiResponse<>(false,ApiResponse.Code.SUCCESS,"Update successful",provider);
        }catch (Exception ex){
            log.error("::Error updateServiceProvider {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> fetchAllServiceProvider(String token) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            List<ServiceProvider> serviceProviderList = serviceProviderRepository.findAllByIsDeleted(false);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Providers fetched....",serviceProviderList);
        }catch (Exception ex){
            log.error("::Error fetchAllServiceProvider {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> updateBiller(String token, Long id, boolean isActive) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProviderBiller> biller = serviceProviderBillerRepository.findByIdAndIsDeleted(id,false);
            if(!biller.isPresent())
                return new ApiResponse<>(true,ApiResponse.Code.NOT_FOUND,"Biller not found....",null);

            biller.get().setIsActive(isActive);
            biller.get().setModifiedBy(response.getData().getEmail());
            biller.get().setModifiedAt(LocalDateTime.now());
            serviceProviderBillerRepository.saveAndFlush(biller.get());
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Biller updated successful....",null);
        }catch (Exception ex){
            log.error("::Error updateBiller {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> updateBillerProduct(String token, Long id, BigDecimal amount, boolean isActive) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProviderProduct> product = serviceProviderProductRepository.findByIdAndIsDeleted(id,false);
            if(!product.isPresent())
                return new ApiResponse<>(true,ApiResponse.Code.NOT_FOUND,"Product not found....",null);

            product.get().setIsActive(isActive);
            product.get().setModifiedBy(response.getData().getEmail());
            product.get().setModifiedAt(LocalDateTime.now());
            if(amount != null){
                if(!amount.equals(BigDecimal.ZERO))
                    product.get().setAmount(amount);
            }

            serviceProviderProductRepository.saveAndFlush(product.get());
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Product updated successful....",null);
        }catch (Exception ex){
            log.error("::Error updateBillerProduct {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> updateBillerProductBundle(String token, Long id, BigDecimal amount, boolean isActive) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProviderProductBundle> bundle = serviceProviderProductBundleRepository.findByIdAndIsDeleted(id,false);
            if(!bundle.isPresent())
                return new ApiResponse<>(true,ApiResponse.Code.NOT_FOUND,"Product not found....",null);

            bundle.get().setIsActive(isActive);
            bundle.get().setModifiedBy(response.getData().getEmail());
            bundle.get().setModifiedAt(LocalDateTime.now());
            if(amount != null){
                if(!amount.equals(BigDecimal.ZERO))
                    bundle.get().setAmount(amount);
            }
            serviceProviderProductBundleRepository.saveAndFlush(bundle.get());
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Bundle updated successful....",null);
        }catch (Exception ex){
            log.error("::Error updateBillerProductBundle {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> updateBillerCategory(String token, Long id, boolean isActive) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProviderCategory> category = serviceProviderCategoryRepository.findByIdAndIsDeleted(id,false);
            if(!category.isPresent())
                return new ApiResponse<>(true,ApiResponse.Code.NOT_FOUND,"Product not found....",null);

            category.get().setIsActive(isActive);
            category.get().setModifiedBy(response.getData().getEmail());
            category.get().setModifiedAt(LocalDateTime.now());
            serviceProviderCategoryRepository.saveAndFlush(category.get());

            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Provider category updated successful....",null);
        }catch (Exception ex){
            log.error("::Error updateBillerCategory {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> updateCategory(String token, Long id, boolean isActive) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<Category> category = categoryRepository.findByIdAndIsDeleted(id,false);
            if(!category.isPresent())
                return new ApiResponse<>(true,ApiResponse.Code.NOT_FOUND,"Category not found....",null);

            category.get().setIsActive(isActive);
            category.get().setModifiedBy(response.getData().getEmail());
            category.get().setModifiedAt(LocalDateTime.now());
            categoryRepository.saveAndFlush(category.get());

            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Category updated successful....",null);
        }catch (Exception ex){
            log.error("::Error updateCategory {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> activateServiceProvider(String token,Long id, boolean isActive) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsDeleted(id,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Provider not found",null);

            serviceProvider.get().setIsActive(isActive);
            serviceProvider.get().setModifiedAt(LocalDateTime.now());
            serviceProvider.get().setModifiedBy(response.getData().getEmail());
            serviceProviderRepository.save(serviceProvider.get());
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Update was successful",serviceProvider.get());
        }catch (Exception ex){
            log.error("::Error activateServiceProvider {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }


    @Override
    public ApiResponse<?> createBillProviderCharges(String token,Long serviceProviderId, Long serviceProviderCategoryId, CreateChargeDto createChargeDto) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider not found",null);

            Optional<ServiceProviderCategory> serviceProviderCategory = serviceProviderCategoryRepository.findByIdAndServiceProviderAndIsActiveAndIsDeleted(serviceProviderCategoryId,serviceProvider.get(),true,false);
            if(!serviceProviderCategory.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider category not found",null);

            Optional<BillProviderCharge> billProviderCharge = billProviderChargeRepository.findByServiceProviderAndServiceProviderCategoryAndIsActiveAndIsDeleted(serviceProvider.get(),serviceProviderCategory.get(),true,false);
            if(billProviderCharge.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Charges already set for this service provider category",null);

            BillProviderCharge charge = new BillProviderCharge();
            BeanUtils.copyProperties(createChargeDto,charge);
            charge.setCreatedBy(response.getData().getEmail());
            charge.setModifiedBy(response.getData().getEmail());
            charge.setServiceProvider(serviceProvider.get());
            charge.setServiceProviderCategory(serviceProviderCategory.get());
            billProviderChargeRepository.saveAndFlush(charge);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Charges created successful",charge);
        }catch (Exception ex){
            log.error("::Error createBillProviderCharges {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }


    @Override
    public ApiResponse<?> updateBillProviderCharges(String token,Long id, CreateChargeDto updateChargeDto) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<BillProviderCharge> billProviderCharge = billProviderChargeRepository.findByIdAndIsActiveAndIsDeleted(id,true,false);
            if(!billProviderCharge.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service charges not found",null);

            BeanUtils.copyProperties(updateChargeDto, billProviderCharge.get());
            billProviderCharge.get().setModifiedAt(LocalDateTime.now());
            billProviderCharge.get().setModifiedBy(response.getData().getEmail());
            billProviderChargeRepository.save(billProviderCharge.get());
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Update was successful",billProviderCharge.get());
        }catch (Exception ex){
            log.error("::Error updateBillProviderCharges {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }


    @Override
    public ApiResponse<?> activateCategory(String token,Long id, boolean isActive) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<Category> category = categoryRepository.findByIdAndIsDeleted(id,false);
            if(!category.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Bill category not found",null);

            category.get().setIsActive(isActive);
            category.get().setModifiedAt(LocalDateTime.now());
            category.get().setModifiedAt(LocalDateTime.now());
            categoryRepository.save(category.get());

            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Update was successful",category.get());
        }catch (Exception ex){
            log.error("::Error activateBillCategory {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }


    @Override
    public ApiResponse<?> createCategory(String token, BillCategoryName name, String  description) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<Category> category = categoryRepository.findByNameAndIsDeleted(name.name(), false);
            if(category.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Category already exist, you can activate it back",null);

            Category billCategory = new Category();
            billCategory.setModifiedBy(response.getData().getEmail());
            billCategory.setCreatedBy(response.getData().getEmail());
            billCategory.setName(name.name());
            billCategory.setDescription(description);
//            BeanUtils.copyProperties(request,billCategory);
            categoryRepository.save(billCategory);

            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Bill Category created",billCategory);
        }catch (Exception ex){
            log.error("::Error createBillCategory {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> fetchAllCategory(String token) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            List<Category> categoryList = categoryRepository.findAllByIsDeleted( false);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Category fetched...",categoryList);
        }catch (Exception ex){
            log.error("::Error fetchAllBillCategory {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }


//    @Override
//    public ApiResponse<?> createBillerCategory(String token, String name, String description, Long categoryId, boolean isActive) {
//        try {
//            AuthResponse response = authProxy.validateUserToken(token);
//            if(!response.getStatus().equals(Boolean.TRUE))
//                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);
//
//            if(!response.getData().isAdmin())
//                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);
//
//            Optional<Category> category = categoryRepository.findByIdAndIsActiveAndIsDeleted(categoryId,true,false);
//            if(!category.isPresent())
//                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Category not found",null);
//
//            Optional<BillerCategory> bCategory = billerCategoryRepository.findByNameAndCategoryAndIsActiveAndIsDeleted(name,category.get(),true,false);
//            if(bCategory.isPresent())
//                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Biller category with name already exist",null);
//
//            BillerCategory billerCategory = new BillerCategory();
//            billerCategory.setCreatedAt(LocalDateTime.now());
//            billerCategory.setModifiedAt(LocalDateTime.now());
//            billerCategory.setCreatedBy(response.getData().getEmail());
//            billerCategory.setModifiedBy(response.getData().getEmail());
//            billerCategory.setName(name);
//            billerCategory.setDescription(description);
//            billerCategory.setIsActive(isActive);
//            billerCategory.setCategory(category.get());
//            billerCategoryRepository.save(billerCategory);
//
//            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Biller category created successful",billerCategory);
//        }catch (Exception ex){
//            log.error("::Error createBillerCategory {}",ex.getLocalizedMessage());
//            ex.printStackTrace();
//            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
//        }
//    }


//    @Override
//    public ApiResponse<?> createBillerProduct(String token, BillerProductDto billerProductDto) {
//        try {
//            AuthResponse response = authProxy.validateUserToken(token);
//            if(!response.getStatus().equals(Boolean.TRUE))
//                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);
//
//            if(!response.getData().isAdmin())
//                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);
//
//            Optional<BillerCategory> billerCategory = billerCategoryRepository.findByIdAndIsActiveAndIsDeleted(billerProductDto.getBillerCategoryId(),true,false);
//            if(!billerCategory.isPresent())
//                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Biller category not found",null);
//
//            Optional<BillerProduct> bProduct = billerProductRepository.findByNameAndBillerCategoryAndIsActiveAndIsDeleted(billerProductDto.getName(),billerCategory.get(),true,false );
//            if(bProduct.isPresent())
//                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Biller product with name already exist",null);
//
//            BillerProduct billerProduct = new BillerProduct();
//            billerProduct.setCreatedAt(LocalDateTime.now());
//            billerProduct.setModifiedAt(LocalDateTime.now());
//            billerProduct.setCreatedBy(response.getData().getEmail());
//            billerProduct.setModifiedBy(response.getData().getEmail());
//            BeanUtils.copyProperties(billerProductDto,billerProduct);
//            billerProduct.setBillerCategory(billerCategory.get());
//            billerProductRepository.save(billerProduct);
//
//            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Biller product successfully created",billerProduct);
//        }catch (Exception ex){
//            log.error("::Error createBillerProduct {}",ex.getLocalizedMessage());
//            ex.printStackTrace();
//            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
//        }
//    }


//    @Override
//    public ApiResponse<?> createBillerProductBundle(String token, BigDecimal amount, String name, Long billerProductId, boolean isActive) {
//        try {
//            AuthResponse response = authProxy.validateUserToken(token);
//            if(!response.getStatus().equals(Boolean.TRUE))
//                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);
//
//            if(!response.getData().isAdmin())
//                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);
//
//            Optional<BillerProduct> bProduct = billerProductRepository.findByIdAndIsActiveAndIsDeleted(billerProductId,true,false );
//            if(!bProduct.isPresent())
//                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Biller product not found",null);
//
//            Optional<BillerProductBundle> billerProductBundle = billerProductBundleRepository.findByNameAndBillerProductAndIsActiveAndIsDeleted(name,bProduct.get(),true,false );
//            if(billerProductBundle.isPresent())
//                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Biller product bundle already exist",null);
//
//            BillerProductBundle bundle = new BillerProductBundle();
//            bundle.setCreatedAt(LocalDateTime.now());
//            bundle.setModifiedAt(LocalDateTime.now());
//            bundle.setCreatedBy(response.getData().getEmail());
//            bundle.setModifiedBy(response.getData().getEmail());
//            bundle.setName(name);
//            bundle.setBillerProduct(bProduct.get());
//            bundle.setIsDeleted(isActive);
//            if(amount != null)
//                bundle.setAmount(amount);
//            billerProductBundleRepository.save(bundle);
//
//            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Biller product bundle created successful",bundle);
//        }catch (Exception ex){
//            log.error("::Error createBillerProductBundle {}",ex.getLocalizedMessage());
//            ex.printStackTrace();
//            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
//        }
//    }



//    @Override
//    public ApiResponse<?> fetchAllBillerCategory(String token) {
//        try {
//            AuthResponse response = authProxy.validateUserToken(token);
//            if(!response.getStatus().equals(Boolean.TRUE))
//                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);
//
//            if(!response.getData().isAdmin())
//                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);
//
//            List<BillerCategory> billerCategoryList = billerCategoryRepository.findAllByIsActiveAndIsDeleted(true, false);
//            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Biller category fetched...", billerCategoryList);
//        }catch (Exception ex){
//            log.error("::Error fetchAllBillerCategory {}",ex.getLocalizedMessage());
//            ex.printStackTrace();
//            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
//        }
//    }



//    @Override
//    public ApiResponse<?> fetchAllBillerProduct(String token) {
//        try {
//            AuthResponse response = authProxy.validateUserToken(token);
//            if(!response.getStatus().equals(Boolean.TRUE))
//                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);
//
//            if(!response.getData().isAdmin())
//                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);
//
//            List<BillerProduct> billerProductList = billerProductRepository.findAllByIsActiveAndIsDeleted(true,false);
//            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Biller product fetched...", billerProductList);
//        }catch (Exception ex){
//            log.error("::Error fetchAllBillerProduct {}",ex.getLocalizedMessage());
//            ex.printStackTrace();
//            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
//        }
//    }



//    @Override
//    public ApiResponse<?> fetchAllBillerProductBundle(String token, Long billerProductId) {
//        try {
//            AuthResponse response = authProxy.validateUserToken(token);
//            if(!response.getStatus().equals(Boolean.TRUE))
//                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);
//
//            if(!response.getData().isAdmin())
//                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);
//
//            Optional<BillerProduct> billerProduct = billerProductRepository.findByIdAndIsActiveAndIsDeleted(billerProductId,true,false);
//            if(!billerProduct.isPresent())
//                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Biller product not found...", null);
//
//            List<BillerProductBundle> billerProductBundleList = billerProductBundleRepository.findAllByBillerProductAndIsActiveAndIsDeleted(billerProduct.get(),true,false);
//            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Biller product bundle fetched...", billerProductBundleList);
//        }catch (Exception ex){
//            log.error("::Error fetchAllBillerProductBundle {}",ex.getLocalizedMessage());
//            ex.printStackTrace();
//            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
//        }
//    }


    @Override
    public ApiResponse<?> createTransactionHistory(TransactionDto transactionDto,String email) {
        try {
            TransactionHistory history = new TransactionHistory();
            history.setModifiedBy(email);
            history.setCreatedBy(email);
            BeanUtils.copyProperties(transactionDto,history);
            transactionHistoryRepository.save(history);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Success",null);
        }catch (Exception ex){
            log.error("::Error createTransactionHistory {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

//    @Override
//    public ApiResponse<?> filterAllBillerCategory(String token, Long categoryId, int pageNo, int pageSize) {
//        try {
//            AuthResponse response = authProxy.validateUserToken(token);
//            if(!response.getStatus().equals(Boolean.TRUE))
//                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);
//
//            if(!response.getData().isAdmin())
//                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);
//
//            int page = pageNo - 1;
//            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC,"createdAt"));
//            Page<BillerCategory> billerCategoryList;
//            if(categoryId != null){
//                Optional<Category> category = categoryRepository.findByIdAndIsActiveAndIsDeleted(categoryId,true,false);
//                if(!category.isPresent())
//                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Category not found",null);
//
//                billerCategoryList = billerCategoryRepository.findAllByCategoryAndIsActiveAndIsDeleted(category.get(),true,false,pageable);
//                return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Biller category fetched...",billerCategoryList);
//            }
//            billerCategoryList = billerCategoryRepository.findAllByIsActiveAndIsDeleted(true, false,pageable);
//            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Biller category fetched....",billerCategoryList);
//        }catch (Exception ex){
//            log.error("::Error filterAllBillerCategory {}",ex.getLocalizedMessage());
//            ex.printStackTrace();
//            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
//        }
//    }

//    @Override
//    public ApiResponse<?> filterAllBillerProduct(String token, Long billerCategoryId, int pageNo, int pageSize) {
//        try {
//            AuthResponse response = authProxy.validateUserToken(token);
//            if(!response.getStatus().equals(Boolean.TRUE))
//                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);
//
//            if(!response.getData().isAdmin())
//                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);
//
//            int page = pageNo - 1;
//            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC,"createdAt"));
//            Page<BillerProduct> billerProductList;
//            if(billerCategoryId != null){
//                Optional<BillerCategory> billerCategory = billerCategoryRepository.findByIdAndIsActiveAndIsDeleted(billerCategoryId,true,false);
//                if(!billerCategory.isPresent())
//                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Bill category not found",null);
//
//                billerProductList = billerProductRepository.findAllByBillerCategoryAndIsActiveAndIsDeleted(billerCategory.get(),true,false,pageable);
//                return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Biller category fetched...",billerProductList);
//            }
//            billerProductList = billerProductRepository.findAllByIsActiveAndIsDeleted(true, false,pageable);
//            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Biller product fetched....",billerProductList);
//        }catch (Exception ex){
//            log.error("::Error filterAllBillerProduct {}",ex.getLocalizedMessage());
//            ex.printStackTrace();
//            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
//        }
//    }

    @Override
    public ApiResponse<?> filterAllCategory(String token, int pageNo, int pageSize) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            int page  = pageNo - 1;
            Pageable pageable =  PageRequest.of(page,pageSize,Sort.by(Sort.Direction.DESC,"createdAt"));
            Page<Category> categoryList = categoryRepository.findAllByIsActiveAndIsDeleted(true, false,pageable);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Category fetched...",categoryList);
        }catch (Exception ex){
            log.error("::Error filterAllCategory {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> filterAllServiceProvider(String token, int pageNo, int pageSize) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            int page = pageNo - 1;
            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC,"createdAt"));
            Page<ServiceProvider> serviceProviders = serviceProviderRepository.findAllByIsActiveAndIsDeleted(true, false,pageable);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Service provider fetched....",serviceProviders);
        }catch (Exception ex){
            log.error("::Error filterAllServiceProvider {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

//    @Override
//    public ApiResponse<?> filterAllBillerProductBundle(String token, Long billerProductId, int pageNo, int pageSize) {
//        try {
//            AuthResponse response = authProxy.validateUserToken(token);
//            if(!response.getStatus().equals(Boolean.TRUE))
//                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);
//
//            if(!response.getData().isAdmin())
//                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);
//
//            int page = pageNo - 1;
//            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC,"createdAt"));
//            Page<BillerProductBundle> billerProductBundles;
//            if(billerProductId != null){
//                Optional<BillerProduct> billerProduct = billerProductRepository.findByIdAndIsActiveAndIsDeleted(billerProductId,true,false);
//                if(!billerProduct.isPresent())
//                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Bill product not found",null);
//
//                billerProductBundles = billerProductBundleRepository.findAllByBillerProductAndIsActiveAndIsDeleted(billerProduct.get(),true,false,pageable);
//                return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Biller product bundles fetched...",billerProductBundles);
//            }
//            billerProductBundles = billerProductBundleRepository.findAllByIsActiveAndIsDeleted(true, false,pageable);
//            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Biller product bundles fetched....",billerProductBundles);
//        }catch (Exception ex){
//            log.error("::Error filterAllBillerProductBundle {}",ex.getLocalizedMessage());
//            ex.printStackTrace();
//            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
//        }
//    }

    @Override
    public ApiResponse<?> createServiceProviderCategory(String token, Long serviceProviderId, String name, String description, String type) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true,false);
            if(!serviceProvider.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider not found", null);

            Optional<ServiceProviderCategory> serviceProviderCategory = serviceProviderCategoryRepository.findByNameAndServiceProviderAndIsActiveAndIsDeleted(name,serviceProvider.get(),true,false);
            if(serviceProviderCategory.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Service provider category name already existed",null);

            ServiceProviderCategory providerCategory = new ServiceProviderCategory();
            providerCategory.setModifiedBy(response.getData().getEmail());
            providerCategory.setCreatedBy(response.getData().getEmail());
            providerCategory.setName(name);
            providerCategory.setType(type);
            providerCategory.setDescription(description);
            providerCategory.setServiceProvider(serviceProvider.get());
            serviceProviderCategoryRepository.save(providerCategory);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Service provider category successful created!",providerCategory);
        }catch (Exception ex){
            log.error("::Error createServiceProviderCategory {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> createServiceProviderBiller(String token, Long serviceProviderCategoryId, String name, String description, String type) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProviderCategory> serviceProviderCategory = serviceProviderCategoryRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderCategoryId,true,false);
            if(!serviceProviderCategory.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider category not found",null);

            Optional<ServiceProviderBiller> serviceProviderBiller = serviceProviderBillerRepository.findByNameAndServiceProviderCategoryAndIsActiveAndIsDeleted(name,serviceProviderCategory.get(),true,false);
            if(serviceProviderBiller.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Service provider biller already exist!",null);

            ServiceProviderBiller providerBiller = new ServiceProviderBiller();
            providerBiller.setCreatedBy(response.getData().getEmail());
            providerBiller.setModifiedBy(response.getData().getEmail());
            providerBiller.setName(name);
            providerBiller.setDescription(description);
            providerBiller.setServiceProviderCategory(serviceProviderCategory.get());
            providerBiller.setType(type);
            serviceProviderBillerRepository.save(providerBiller);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Service provider biller successful created!",providerBiller);
        }catch (Exception ex){
            log.error("::Error createServiceProviderBiller {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> createServiceProviderProduct(String token, Long serviceProviderBillerId, String name, String description, String type, boolean hasBundle, boolean hasTokenValidation) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProviderBiller> serviceProviderBiller = serviceProviderBillerRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderBillerId,true,false);
            if(!serviceProviderBiller.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider biller not found!",null);

            Optional<ServiceProviderProduct> serviceProviderProduct = serviceProviderProductRepository.findByNameAndServiceProviderBillerAndIsActiveAndIsDeleted(name,serviceProviderBiller.get(),true,false);
            if(serviceProviderProduct.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Service provider product already exist!",null);


            ServiceProviderProduct providerProduct = new ServiceProviderProduct();
            providerProduct.setCreatedBy(response.getData().getEmail());
            providerProduct.setModifiedBy(response.getData().getEmail());
            providerProduct.setName(name);
            providerProduct.setDescription(description);
            providerProduct.setServiceProviderBiller(serviceProviderBiller.get());
            providerProduct.setType(type);
            providerProduct.setHasBundle(hasBundle);
            providerProduct.setHasTokenValidation(hasTokenValidation);
            serviceProviderProductRepository.save(providerProduct);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Service provider product successful created!",providerProduct);
        }catch (Exception ex){
            log.error("::Error createServiceProviderProduct {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }


    @Override
    public ApiResponse<?> createServiceProviderProductBundle(String token, Long serviceProviderProductId, BigDecimal amount, String name, String description, String type) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            Optional<ServiceProviderProduct> serviceProviderProduct = serviceProviderProductRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderProductId,true,false);
            if(!serviceProviderProduct.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider product not found!",null);

            Optional<ServiceProviderProductBundle> serviceProviderProductBundle = serviceProviderProductBundleRepository.findByNameAndServiceProviderProductAndIsActiveAndIsDeleted(name,serviceProviderProduct.get(),true,false);
            if(serviceProviderProductBundle.isPresent())
                return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Service provider product bundle already exist",null);

            ServiceProviderProductBundle providerProductBundle = new ServiceProviderProductBundle();
            providerProductBundle.setCreatedBy(response.getData().getEmail());
            providerProductBundle.setModifiedBy(response.getData().getEmail());
            providerProductBundle.setName(name);
            providerProductBundle.setDescription(description);
            providerProductBundle.setServiceProviderProduct(serviceProviderProduct.get());
            if(amount != null)
                providerProductBundle.setAmount(amount);

            serviceProviderProductBundleRepository.save(providerProductBundle);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Service provider product bundle successful created!",providerProductBundle);
        }catch (Exception ex){
            log.error("::Error createServiceProviderProductBundle {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> fetchServiceProviderCategory(String token, Long serviceProviderId, int pageNo, int pageSize) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            int page = pageNo - 1;
            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC,"createdAt"));
            Page<ServiceProviderCategory> serviceProviderCategoryList;
            if(serviceProviderId != null){
                Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true,false);
                if(!serviceProvider.isPresent())
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider not found",null);

                serviceProviderCategoryList = serviceProviderCategoryRepository.findAllByServiceProviderAndIsActiveAndIsDeleted(serviceProvider.get(),true,false,pageable);
                return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Service provider category fetched..",serviceProviderCategoryList);
            }
            serviceProviderCategoryList = serviceProviderCategoryRepository.findAllByIsActiveAndIsDeleted(true,false,pageable);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Service provider category fetched..",serviceProviderCategoryList);
        }catch (Exception ex){
            log.error("::Error fetchServiceProviderCategory {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> fetchServiceProviderBiller(String token, Long serviceProviderCategoryId, int pageNo, int pageSize) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            int page = pageNo - 1;
            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC,"createdAt"));
            Page<ServiceProviderBiller> serviceProviderBillers;
            if(serviceProviderCategoryId != null){
                Optional<ServiceProviderCategory> serviceProviderCategory = serviceProviderCategoryRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderCategoryId,true,false);
                if(!serviceProviderCategory.isPresent())
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider category not found",null);

                serviceProviderBillers = serviceProviderBillerRepository.findAllByServiceProviderCategoryAndIsActiveAndIsDeleted(serviceProviderCategory.get(),true,false,pageable);
                return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Service provider biller fetched..",serviceProviderBillers);
            }
            serviceProviderBillers = serviceProviderBillerRepository.findAllByIsActiveAndIsDeleted(true,false,pageable);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Service provider biller fetched..",serviceProviderBillers);
        }catch (Exception ex){
            log.error("::Error fetchServiceProviderBiller {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> fetchServiceProviderProduct(String token, Long serviceProviderBillerId, int pageNo, int pageSize) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            int page = pageNo - 1;
            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC,"createdAt"));
            Page<ServiceProviderProduct> serviceProviderProducts;
            if(serviceProviderBillerId != null){
                Optional<ServiceProviderBiller> serviceProviderBiller = serviceProviderBillerRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderBillerId,true,false);
                if(!serviceProviderBiller.isPresent())
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider biller not found",null);

                serviceProviderProducts = serviceProviderProductRepository.findAllByServiceProviderBillerAndIsActiveAndIsDeleted(serviceProviderBiller.get(),true,false,pageable);
                return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Service provider product fetched..",serviceProviderProducts);
            }
            serviceProviderProducts = serviceProviderProductRepository.findAllByIsActiveAndIsDeleted(true,false,pageable);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Service provider product fetched..",serviceProviderProducts);
        }catch (Exception ex){
            log.error("::Error fetchServiceProviderProduct {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }


    @Override
    public ApiResponse<?> fetchServiceProviderProductBundle(String token, Long serviceProviderProductId, int pageNo, int pageSize) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            int page = pageNo - 1;
            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC,"createdAt"));
            Page<ServiceProviderProductBundle> serviceProviderProductBundles;
            if(serviceProviderProductId != null){
                Optional<ServiceProviderProduct> serviceProviderProduct = serviceProviderProductRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderProductId,true,false);
                if(!serviceProviderProduct.isPresent())
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider product not found",null);

                serviceProviderProductBundles = serviceProviderProductBundleRepository.findAllByServiceProviderProductAndIsActiveAndIsDeleted(serviceProviderProduct.get(),true,false,pageable);
                return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Service provider bundle fetched..",serviceProviderProductBundles);
            }
            serviceProviderProductBundles = serviceProviderProductBundleRepository.findAllByIsActiveAndIsDeleted(true,false,pageable);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Service provider bundle fetched..",serviceProviderProductBundles);
        }catch (Exception ex){
            log.error("::Error fetchServiceProviderProductBundle {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> fetchBillChargesForProviders(String token, Long serviceProviderId, int pageNo, int pageSize) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            int page = pageNo - 1;
            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC,"createdAt"));
            Page<BillProviderCharge> billProviderCharges;
            if(serviceProviderId != null){
                Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByIdAndIsActiveAndIsDeleted(serviceProviderId,true,false);
                if(!serviceProvider.isPresent())
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider not found",null);

                billProviderCharges = billProviderChargeRepository.findAllByServiceProviderAndIsActiveAndIsDeleted(serviceProvider.get(),true,false,pageable);
                return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Service provider bundle fetched..",billProviderCharges);
            }
            billProviderCharges = billProviderChargeRepository.findAllByIsActiveAndIsDeleted(true,false,pageable);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Bill charges fetched..",billProviderCharges);
        }catch (Exception ex){
            log.error("::Error fetchBillChargesForProviders {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> adminAnalysis(String token) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            HashMap<String, BigDecimal> map = new HashMap<>();
            map.put("successCount", BigDecimal.valueOf(transactionHistoryRepository.countByStatus(PaymentStatus.SUCCESSFUL)));
            map.put("failCount", BigDecimal.valueOf(transactionHistoryRepository.countByStatus(PaymentStatus.FAILED)));
            map.put("errorCount", BigDecimal.valueOf(transactionHistoryRepository.countByStatus(PaymentStatus.ERROR)));
            map.put("totalSuccessAmount", transactionHistoryRepository.sumAmount(PaymentStatus.SUCCESSFUL));
            map.put("totalFailAmount", transactionHistoryRepository.sumAmount(PaymentStatus.FAILED));
            map.put("totalErrorAmount", transactionHistoryRepository.sumAmount(PaymentStatus.ERROR));


            List<ServiceProvider> serviceProviderList = serviceProviderRepository.findAllByIsDeleted(false);
            if(serviceProviderList.size() > 0){
                for (ServiceProvider provider: serviceProviderList){
                    String name = provider.getName();
                    String providerName =  formatNameToCamelCase(name);
                    if(providerName == null)
                        continue;

                    BigDecimal providerAmount = transactionHistoryRepository.sumAmountByProviders(PaymentStatus.SUCCESSFUL,provider.getId());
                    map.put("success"+providerName.trim(),providerAmount);
                    BigDecimal providerAmount2 = transactionHistoryRepository.sumAmountByProviders(PaymentStatus.FAILED,provider.getId());
                    map.put("fail"+providerName.trim(),providerAmount2);
                    BigDecimal providerAmount3 = transactionHistoryRepository.sumAmountByProviders(PaymentStatus.ERROR,provider.getId());
                    map.put("error"+providerName.trim(),providerAmount3);
                }
            }
            CustomObject customObject = new CustomObject();
            customObject.setMap(map);
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Analysis fetched...",customObject);
        }catch (Exception ex){
            log.error("::Error adminAnalysis {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, can not process your request",null);
        }
    }

    @Override
    public ApiResponse<?> fetchOrFilterTransactionHistory(String token,String endDate, String field, String value, int pageNo, int pageSize) {
        try {
            AuthResponse response = authProxy.validateUserToken(token);
            if(!response.getStatus().equals(Boolean.TRUE))
                return new ApiResponse<>(false,ApiResponse.Code.UNAUTHORIZED,"UNAUTHORIZED",null);

            if(!response.getData().isAdmin())
                return new ApiResponse<>(false,ApiResponse.Code.FORBIDDEN,"Oops!\n You don't have access to this resources",null);

            int page = pageNo - 1;
            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC,"createdAt"));
            Page<TransactionHistory> transactionHistories;
            if(field.equalsIgnoreCase("reference")){
                transactionHistories = transactionHistoryRepository.findAllByPaymentReferenceNumber(value,pageable);
            }else if(field.equalsIgnoreCase("status")){
                transactionHistories = transactionHistoryRepository.findAllByStatus(PaymentStatus.valueOf(value.toUpperCase()),pageable);
            }else if(field.equalsIgnoreCase("email")){
                transactionHistories = transactionHistoryRepository.findAllBySenderEmail(value,pageable);
            }else if(field.equalsIgnoreCase("accountNumber")){
                transactionHistories = transactionHistoryRepository.findAllByAccountNumber(value,pageable);
            }else if(field.equalsIgnoreCase("category")){
                transactionHistories = transactionHistoryRepository.findAllByCategoryName(BillCategoryName.valueOf(value.toLowerCase()),pageable);
            }else if(field.equalsIgnoreCase("providerName")){
                Optional<ServiceProvider> provider = serviceProviderRepository.findFirstByNameStartingWithIgnoreCase(value);
                if(!provider.isPresent())
                    return new ApiResponse<>(false,ApiResponse.Code.NOT_FOUND,"Service provider not found",null);

                transactionHistories = transactionHistoryRepository.findAllByServiceProviderBiller_ServiceProviderId(provider.get().getId(), pageable);
            }else if(field.equalsIgnoreCase("date") && endDate != null){
                LocalDateTime start = getLocalDateFormat(value);
                LocalDateTime end = getLocalDateFormat(endDate);
                transactionHistories = transactionHistoryRepository.findAllByCreatedAtBetween(start,end,pageable);
            }else {
                transactionHistories = transactionHistoryRepository.findAll(pageable);
            }
            return new ApiResponse<>(true,ApiResponse.Code.SUCCESS,"Histories fetch...",transactionHistories);
        }catch (Exception ex){
            log.error("::Error adminAnalysis {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false,ApiResponse.Code.BAD_REQUEST,"Oops!\n Something went wrong, "+ex.getLocalizedMessage(),null);
        }
    }


    private LocalDateTime getLocalDateFormat(String value) throws ParseException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(value, formatter);
            return localDateTime;
        }catch (Exception ex){
          log.error("::Error getLocalDateFormat {}",ex.getLocalizedMessage());
          throw new ParseException("Date not properly formatted",0);
        }
    }

    private String formatNameToCamelCase(String name){
        try {
            String[] words = name.split(" ");
            StringBuilder camelCaseName = new StringBuilder();

            for (int i = 0; i < words.length; i++) {
                String word = words[i].toLowerCase();
                if (i > 0) {
                    // Capitalize the first character of each word except the first word
                    word = Character.toUpperCase(word.charAt(0)) + word.substring(1);
                }
                camelCaseName.append(word);
            }

            String camelCaseResult = camelCaseName.toString();
            return camelCaseResult;
        }catch (Exception ex){
            log.error("::Error formatNameToCamelCase {}",ex.getLocalizedMessage());
            return null;
        }
    }

}
