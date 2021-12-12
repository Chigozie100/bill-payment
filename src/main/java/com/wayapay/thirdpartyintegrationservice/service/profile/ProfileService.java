package com.wayapay.thirdpartyintegrationservice.service.profile;

import com.wayapay.thirdpartyintegrationservice.dto.ProfileResponseObject;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

@Data
@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileService {

    private final ProfileFeignClient profileFeignClient;

    public UserProfileResponse getUserProfile(String userName, String token) throws ThirdPartyIntegrationException {
        UserProfileResponse userProfileResponse = null;
        try {
            ResponseEntity<ProfileResponseObject> responseEntity = profileFeignClient.getUserProfile(userName, token);
            ProfileResponseObject infoResponse = responseEntity.getBody();
            userProfileResponse = infoResponse.data;
            log.info("userProfileResponse :: " +userProfileResponse);
        } catch (Exception e) {
            log.error("Unable to generate transaction Id", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }
        return userProfileResponse;
    }
}
