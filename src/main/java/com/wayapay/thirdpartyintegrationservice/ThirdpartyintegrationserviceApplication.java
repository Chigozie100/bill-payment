package com.wayapay.thirdpartyintegrationservice;

import com.wayapay.thirdpartyintegrationservice.interceptors.SwaggerFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ThirdpartyintegrationserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThirdpartyintegrationserviceApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new SwaggerFilter());
        return filterRegistrationBean;
    }

}
