package com.wayapay.thirdpartyintegrationservice.config;

import feign.Response;
import feign.codec.ErrorDecoder;

public class CustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String s, Response response) {
        if (response.body() != null && response.headers().containsKey("Content-Type")
                && response.headers().get("Content-Type").contains("text/html")) {
            // Log or handle HTML response gracefully
            return new RuntimeException("Received HTML response: " + response.status());
        }
        return new ErrorDecoder.Default().decode(s, response);
    }
}
