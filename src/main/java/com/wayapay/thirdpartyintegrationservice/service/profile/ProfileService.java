package com.wayapay.thirdpartyintegrationservice.service.profile;

import com.wayapay.thirdpartyintegrationservice.dto.ApiResponseBody;
import com.wayapay.thirdpartyintegrationservice.dto.ProfileResponseObject;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.auth.AuthFeignClient;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Data
@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileService {

    private final ProfileFeignClient profileFeignClient;

    private final AuthFeignClient authFeignClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

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


    public Profile getProfile(String userName, String token) throws ThirdPartyIntegrationException {
        Profile profile = null;
        try {
            ResponseEntity<ApiResponseBody<Profile>> responseEntity = authFeignClient.getProfile(userName, token);
            ApiResponseBody<Profile> infoResponse = responseEntity.getBody();
            profile = infoResponse.getData();
            log.info("userProfileResponse :: " +profile);

            kafkaTemplate.send("create-user-profile", CommonUtils.getObjectMapper().writeValueAsString(profile));

            log.info(" Sent to topic");
        } catch (Exception e) {
            log.error("Unable to generate transaction Id", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }
        return profile;
    }
}
