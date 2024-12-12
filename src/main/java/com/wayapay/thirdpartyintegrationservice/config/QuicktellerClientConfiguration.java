package com.wayapay.thirdpartyintegrationservice.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class QuicktellerClientConfiguration {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

}
