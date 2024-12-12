package com.wayapay.thirdpartyintegrationservice.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;



@Component
public class AuthenticatedUserImpl implements AuthenticatedUserFacade {


    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    

}