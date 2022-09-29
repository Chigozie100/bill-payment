package com.wayapay.thirdpartyintegrationservice.service.profile;

import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.auth.AuthFeignClient;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Data
@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileService {

    private final ProfileFeignClient profileFeignClient;
    private final ModelMapper modelMapper;
    private final AuthFeignClient authFeignClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public UserProfileResponse getUserProfile(String userName, String token) throws ThirdPartyIntegrationException {
        UserProfileResponse userProfileResponse;
        try {
            ResponseEntity<ProfileResponseObject> responseEntity = profileFeignClient.getUserProfile(userName, token);
            ProfileResponseObject infoResponse = responseEntity.getBody();
            assert infoResponse != null;
            userProfileResponse = Objects.requireNonNull(infoResponse.data);
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
            if(infoResponse !=null) {
                profile = Objects.requireNonNull(infoResponse.getData());


                ProfileDto request = modelMapper.map(profile, ProfileDto.class);
                ReferralDto referralDto = new ReferralDto();
                referralDto.setId("01");
                referralDto.setProfile(request);
                referralDto.setUserId(profile.getUserId());
                referralDto.setReferralCode("43w4s4w45dr45");
                log.info("ReferralCode :: " + referralDto);


                kafkaTemplate.send("create-user-referral", CommonUtils.getObjectMapper().writeValueAsString(referralDto));
            }
            log.info(" Sent to topic");
        } catch (Exception e) {
            log.error("Unable to generate transaction Id", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }
        return profile;
    }

}
