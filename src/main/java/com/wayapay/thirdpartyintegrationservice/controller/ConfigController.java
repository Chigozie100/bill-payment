package com.wayapay.thirdpartyintegrationservice.controller;

import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ErrorResponse;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.responsehelper.SuccessResponse;
import com.wayapay.thirdpartyintegrationservice.service.ConfigService;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.API_V1;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(API_V1+"/config")
public class ConfigController {

    private final ConfigService configService;

    @GetMapping
    public ResponseEntity<ResponseHelper> getActiveThirdParty(){
        try {
            return ResponseEntity.ok(new SuccessResponse(configService.getActiveThirdParty()));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseHelper> getAllThirdParty(){
        return ResponseEntity.ok(new SuccessResponse(ThirdPartyNames.values()));
    }

    @PutMapping("/{thirdPartyName}")
    public ResponseEntity<ResponseHelper> setActiveThirdParty(@PathVariable String thirdPartyName){

        Optional<ThirdPartyNames> thirdPartyNamesOptional = ThirdPartyNames.find(thirdPartyName);
        if (!thirdPartyNamesOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid thirdparty name provided"));
        }

        try {
            configService.setActiveThirdParty(thirdPartyNamesOptional.get());
            return ResponseEntity.ok(new SuccessResponse("Successfully set active thirdParty to "+thirdPartyNamesOptional.get()));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

}
