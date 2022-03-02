package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.ThirdPartyResponse;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.ThirdParty;
import com.wayapay.thirdpartyintegrationservice.repo.ThirdPartyRepo;
import com.wayapay.thirdpartyintegrationservice.responsehelper.SuccessResponse;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ThirdPartyService {

    private final ThirdPartyRepo thirdPartyRepo;

    //getOne
    public SuccessResponse get(Long id) throws ThirdPartyIntegrationException {
        if (Objects.isNull(id)){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_REQUIRED);
        }

        return new SuccessResponse(thirdPartyRepo.findThirdPartyById(id).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_UNKNOWN)));
    }

    //getAll
    public SuccessResponse getAll() throws ThirdPartyIntegrationException {
        try {
            return new SuccessResponse(thirdPartyRepo.findAllThirdParty());
        } catch (Exception exception) {
            log.error("Unable to fetch all aggregators ",exception);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch all aggregators");
        }
    }

    //toggle
    public SuccessResponse toggle(Long id) throws ThirdPartyIntegrationException {
        if (Objects.isNull(id)){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_REQUIRED);
        }

        ThirdParty thirdParty = thirdPartyRepo.findById(id).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_UNKNOWN));
        thirdParty.setActive(!thirdParty.isActive());
        thirdParty = thirdPartyRepo.save(thirdParty);
        // auto enable all the biller under this aggregator

        return new SuccessResponse(thirdParty.isActive() ? "successfully activated" : "successfully deactivated", new ThirdPartyResponse(thirdParty.getId(), thirdParty.getThirdPartyNames(), thirdParty.isActive()));
    }

    //sync
    public SuccessResponse syncAggregator() throws ThirdPartyIntegrationException {

        try {
            List<ThirdPartyNames> thirdPartyNames = Arrays.stream(ThirdPartyNames.values()).collect(Collectors.toList());
            List<ThirdParty> allNotInList = thirdPartyRepo.findAllNotInList(thirdPartyNames);
            if (!allNotInList.isEmpty()) {
                thirdPartyRepo.deleteInBatch(allNotInList);
            }

            List<ThirdPartyNames> allInList = thirdPartyRepo.findAllThirdPartyNames();
            List<ThirdParty> newAggregatorToBeProfiled = thirdPartyNames.stream().filter(name -> !allInList.contains(name)).map(ThirdParty::new).collect(Collectors.toList());
            if (!newAggregatorToBeProfiled.isEmpty()) {
                thirdPartyRepo.saveAll(newAggregatorToBeProfiled);
            }
            return new SuccessResponse(SYNCED_SUCCESSFULLY);
        } catch (Exception exception) {
            log.error("Unable to sync aggregators", exception);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }

    }

    public boolean isAggregatorIdValid(Long aggregatorId) throws ThirdPartyIntegrationException {
        try {
            return thirdPartyRepo.existsById(aggregatorId);
        } catch (Exception exception) {
            log.error("Unable to confirm if aggregatorId is valid or not valid", exception);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }
    }

    public Optional<ThirdParty> findById(Long aggregatorId){
        try {
            return thirdPartyRepo.findById(aggregatorId);
        } catch (Exception exception) {
            log.error("Unable to fetch aggregator by Id");
            return Optional.empty();
        }
    }

    public List<ThirdParty> findAll(){
        try {
            return thirdPartyRepo.findAll();
        } catch (Exception exception) {
            log.error("unable to fetch all aggregator");
            return new ArrayList<>();
        }
    }

}
