package com.wayapay.thirdpartyintegrationservice;

import com.wayapay.thirdpartyintegrationservice.interceptors.SwaggerFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

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
