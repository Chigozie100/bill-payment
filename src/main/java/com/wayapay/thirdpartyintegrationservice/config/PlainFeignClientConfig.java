package com.wayapay.thirdpartyintegrationservice.config;

import com.wayapay.thirdpartyintegrationservice.v2.proxyclient.LoginProxy;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = {LoginProxy.class})
public class PlainFeignClientConfig {


}
