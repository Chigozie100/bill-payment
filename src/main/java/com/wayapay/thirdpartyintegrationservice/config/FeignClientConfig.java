package com.wayapay.thirdpartyintegrationservice.config;

import com.wayapay.thirdpartyintegrationservice.interceptors.FeignClientInterceptor;
import com.wayapay.thirdpartyintegrationservice.v2.proxyclient.AuthProxy;
import com.wayapay.thirdpartyintegrationservice.v2.proxyclient.WalletProxy;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.BaxiProxy;
import com.wayapay.thirdpartyintegrationservice.v2.service.notification.NotificationFeignClient;
import com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.QuickTellerProxy;
import feign.RequestInterceptor;
import org.apache.http.entity.ContentType;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = {AuthProxy.class, BaxiProxy.class, WalletProxy.class, QuickTellerProxy.class, NotificationFeignClient.class})
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor(){
        return new FeignClientInterceptor();
    }

}
