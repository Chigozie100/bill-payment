package com.wayapay.thirdpartyintegrationservice.service;

import com.google.gson.JsonSyntaxException;
import com.wayapay.thirdpartyintegrationservice.dto.NewWalletResponse;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.wallet.WalletFeignClient;
import com.wayapay.thirdpartyintegrationservice.util.GsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Slf4j
@RequiredArgsConstructor
@Service
public class WalletService {

    private final WalletFeignClient walletFeignClient;

    public NewWalletResponse getNewDefaultWallet(String userId, String token) throws ThirdPartyIntegrationException {

        String user;

        ResponseEntity<String> response = null;
        try {
            response =  walletFeignClient.getDefaultWallet(userId, token);

            System.out.println("inside getNewDefaultWallet " + response);

            if (response.getStatusCode().isError()) {
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, response.getStatusCode().toString());
            }
            user = response.getBody();
            JSONObject jsonpObject = new JSONObject(user);
            String json = jsonpObject.getJSONObject("data").toString();
            System.out.println("inside json " + json);
            NewWalletResponse mainWalletResponse = GsonUtils.cast(json, NewWalletResponse.class);
            System.out.println("inside json  mainWalletResponse " + mainWalletResponse);
            return mainWalletResponse;

        } catch (RestClientException | JsonSyntaxException | ThirdPartyIntegrationException | JSONException e) {
            System.out.println("Error is here " + e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
    }

}
