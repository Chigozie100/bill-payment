package com.wayapay.thirdpartyintegrationservice.service;

import com.google.gson.JsonSyntaxException;
import com.wayapay.thirdpartyintegrationservice.dto.*;
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

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class WalletService implements WalletFeignClient {

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

    @Override
    public TransactionRequest transferToUser(TransferFromWalletPojo transfer, String token) {
        return null;
    }

    @Override
    public List<MainWalletResponse> getWalletById(Long userId, String token) {
        return null;
    }

    @Override
    public MainWalletResponse getDefaultWallet(String token) {
        return null;
    }

    @Override
    public ResponseEntity<String> getDefaultWallet(String userId, String token) {

        return null;
    }

    @Override
    public ResponseEntity<String> transferFromUserToWaya(TransferFromWalletPojo transfer, String token) {
        return null;
    }

    @Override
    public ResponseEntity<String> getUserCommissionWallet(String userId, String token) {
        return null;
    }

    @Override
    public ResponseEntity<String> getWayaCommissionWallet(String token) {
        return null;
    }

    @Override
    public ResponseEntity<String> getWayaOfficialWallet(String token) {
        return null;
    }

    @Override
    public ResponseEntity<String> wayaAdminAddCommissionWalletToCoUser(String token) {
        return null;
    }

    @Override
    public TransactionRequest adminReversFundToUser(TransferFromWalletToWallet transfer, String token) {
        return null;
    }
}
