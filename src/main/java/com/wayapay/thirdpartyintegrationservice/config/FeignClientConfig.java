package com.wayapay.thirdpartyintegrationservice.config;

import com.wayapay.thirdpartyintegrationservice.service.baxi.BaxiFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.interswitch.QuickTellerFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.itex.ItexFeignClient;
import feign.RequestInterceptor;
import org.apache.http.entity.ContentType;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
@EnableFeignClients(clients = {ItexFeignClient.class, BaxiFeignClient.class, QuickTellerFeignClient.class})
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor(){
        return requestTemplate -> requestTemplate.header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
    }

}
