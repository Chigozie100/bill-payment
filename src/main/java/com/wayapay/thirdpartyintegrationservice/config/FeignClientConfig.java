package com.wayapay.thirdpartyintegrationservice.config;

import com.wayapay.thirdpartyintegrationservice.service.auth.AuthFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.baxi.BaxiFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.commission.CommissionFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.dispute.DisputeServiceFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.interswitch.QuickTellerFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.itex.ItexFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.logactivity.LogFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.notification.NotificationFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.profile.ProfileFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.wallet.WalletFeignClient;
import feign.RequestInterceptor;
import org.apache.http.entity.ContentType;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
@EnableFeignClients(clients = {ItexFeignClient.class, BaxiFeignClient.class, QuickTellerFeignClient.class, DisputeServiceFeignClient.class, WalletFeignClient.class, AuthFeignClient.class, ProfileFeignClient.class, NotificationFeignClient.class, LogFeignClient.class, CommissionFeignClient.class})
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor(){
        return requestTemplate -> requestTemplate.header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
    }

}
