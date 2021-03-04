package com.wayapay.thirdpartyintegrationservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileDetailsService implements UserDetailsService {

    private final JwtTokenHelper jwtTokenHelper;

    public UserDetails loadUserByUsername(String token) {
        if (Objects.isNull(token)){
            throw new UsernameNotFoundException("Invalid Token provided");
        }
        return new ProfileDetails(Collections.singletonList(jwtTokenHelper.getRoleFromToken(token)), jwtTokenHelper.getUsernameFromToken(token));
    }

}