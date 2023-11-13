package com.wayapay.thirdpartyintegrationservice.config;

import org.springframework.security.core.Authentication;


public interface AuthenticatedUserFacade {
	Authentication getAuthentication();
}
