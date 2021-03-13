package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.BillerConsumerFeeRequest;
import com.wayapay.thirdpartyintegrationservice.dto.BillerConsumerFeeResponse;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.BillerConsumerFee;
import com.wayapay.thirdpartyintegrationservice.repo.BillerConsumerFeeRepo;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.responsehelper.SuccessResponse;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import com.wayapay.thirdpartyintegrationservice.util.FeeType;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillerConsumerFeeService {

    private final BillerConsumerFeeRepo billerConsumerFeeRepo;

    private static final String PLACE_HOLDER = "{}";
    private static final String BILLER_PLACE_HOLDER = "{billerName}";
    private static final String BILLER_ALREADY_EXIST = PLACE_HOLDER+" - "+BILLER_PLACE_HOLDER+" fee have been configured";
    private static final String ID_IS_REQUIRED = "Id is required";

    //create
    public ResponseHelper create(BillerConsumerFeeRequest request) throws ThirdPartyIntegrationException {
        //validate thirdParty and Biller
        if (doesItAlreadyExist(request.getThirdPartyName(), request.getBiller(), null)) {
            log.error(BILLER_ALREADY_EXIST.replace(BILLER_PLACE_HOLDER, request.getBiller()), request.getThirdPartyName());
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, BILLER_ALREADY_EXIST.replace(PLACE_HOLDER,request.getThirdPartyName()).replace(BILLER_PLACE_HOLDER, request.getBiller()));
        }

        //store
        BillerConsumerFee billerConsumerFee = generate(new BillerConsumerFee(), request);
        billerConsumerFee = save(billerConsumerFee);
        return new SuccessResponse(new BillerConsumerFeeResponse(billerConsumerFee));
    }

    //update
    public ResponseHelper update(BillerConsumerFeeRequest request) throws ThirdPartyIntegrationException {

        if (Objects.isNull(request.getId())){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_REQUIRED);
        }

        //validate thirdParty and Biller
        if (doesItAlreadyExist(request.getThirdPartyName(), request.getBiller(), request.getId())) {
            log.error(BILLER_ALREADY_EXIST.replace(BILLER_PLACE_HOLDER, request.getBiller()), request.getThirdPartyName());
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, BILLER_ALREADY_EXIST.replace(PLACE_HOLDER,request.getThirdPartyName()).replace(BILLER_PLACE_HOLDER, request.getBiller()));
        }

        //store
        BillerConsumerFee billerConsumerFee = getBillerConsumerFeeById(request.getId());
        generate(billerConsumerFee, request);
        billerConsumerFee = save(billerConsumerFee);
        return new SuccessResponse(new BillerConsumerFeeResponse(billerConsumerFee));
    }

    //findAll
    public ResponseHelper getAll(){
        return new SuccessResponse(billerConsumerFeeRepo.findAllConfigurations());
    }

    //findById
    public ResponseHelper getById(Long id) throws ThirdPartyIntegrationException {
        if (Objects.isNull(id)){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_REQUIRED);
        }
        return new SuccessResponse(getBillerConsumerFeeById(id));
    }

    //toggle
    public ResponseHelper toggle(Long id) throws ThirdPartyIntegrationException {
        if (Objects.isNull(id)){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_REQUIRED);
        }
        BillerConsumerFee billerConsumerFee = getBillerConsumerFeeById(id);
        billerConsumerFee.setActive(!billerConsumerFee.isActive());
        billerConsumerFee = save(billerConsumerFee);
        return new SuccessResponse(new BillerConsumerFeeResponse(billerConsumerFee));
    }

    private boolean doesItAlreadyExist(String thirdPartyName, String biller, Long id){
        if (Objects.isNull(id)){
            return billerConsumerFeeRepo.countByThirdPartyNameAndBiller(ThirdPartyNames.valueOf(thirdPartyName), biller) > 0;
        }
        return billerConsumerFeeRepo.countByThirdPartyNameAndBillerNotId(ThirdPartyNames.valueOf(thirdPartyName), biller, id) > 0;
    }

    private BillerConsumerFee generate(BillerConsumerFee billerConsumerFee, BillerConsumerFeeRequest request){
        billerConsumerFee.setBiller(request.getBiller());
        billerConsumerFee.setFeeType(FeeType.valueOf(request.getFeeType()));
        billerConsumerFee.setMaxFixedValueWhenPercentage(request.getMaxFixedValueWhenPercentage());
        billerConsumerFee.setThirdPartyName(ThirdPartyNames.valueOf(request.getThirdPartyName()));
        billerConsumerFee.setValue(request.getValue());
        return billerConsumerFee;
    }

    private BillerConsumerFee getBillerConsumerFeeById(Long id) throws ThirdPartyIntegrationException {
        return billerConsumerFeeRepo.findById(id).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid id provided"));
    }

    private BillerConsumerFee save(BillerConsumerFee billerConsumerFee) throws ThirdPartyIntegrationException {
        try {
            return billerConsumerFeeRepo.save(billerConsumerFee);
        } catch (Exception exception) {
            log.error("Unable to save biller consumer fee", exception);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }
    }

    public BigDecimal getFee(BigDecimal amount, ThirdPartyNames thirdPartyNames, String biller){
        Optional<BillerConsumerFee> billerConsumerFeeOptional = billerConsumerFeeRepo.findByThirdPartyNameAndBiller(thirdPartyNames, biller);
        if (!billerConsumerFeeOptional.isPresent()){
            return BigDecimal.ZERO;
        }

        BillerConsumerFee billerConsumerFee = billerConsumerFeeOptional.get();
        if (billerConsumerFee.getFeeType() == FeeType.FIXED) {
            return billerConsumerFee.getValue();
        }

        BigDecimal multiplyAmount = billerConsumerFee.getValue().multiply(amount);
        return isGreaterThan(multiplyAmount, billerConsumerFee.getMaxFixedValueWhenPercentage()) ? billerConsumerFee.getMaxFixedValueWhenPercentage() : multiplyAmount;
    }

    private boolean isGreaterThan(BigDecimal amount1, BigDecimal amount2){
        return amount1.compareTo(amount2) > 0;
    }

}
