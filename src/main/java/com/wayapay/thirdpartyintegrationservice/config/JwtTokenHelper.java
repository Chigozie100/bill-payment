package com.wayapay.thirdpartyintegrationservice.config;

import com.wayapay.thirdpartyintegrationservice.v2.dto.request.AuthResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.UserDetail;
import com.wayapay.thirdpartyintegrationservice.v2.proxyclient.AuthProxy;
import feign.FeignException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.ROLE;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenHelper implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    private final AuthProxy authFeignClient;

    @Value("${jwt.secret}")
    private String secret;

    /**
     * retrieve username from jwt token
     * @param userDetail userObject from authService
     * @return subject
     */
    public String getUsernameFromToken(UserDetail userDetail) {
//        return getClaimFromToken(token, Claims::getSubject);
        return String.valueOf(userDetail.getId());
    }

    public Optional<UserDetail> getUserDetail(String token){
        if (Objects.isNull(token)){
            return Optional.empty();
        }
        Optional<AuthResponse> authResponseOptional = Optional.empty();
        try {
            authResponseOptional = Optional.of(authFeignClient.validateUserToken(token));
        } catch (FeignException exception) {
            log.error("Unable to validate token : ", exception);
            return Optional.empty();
        }
        return Optional.of(authResponseOptional.orElse(new AuthResponse()).getData());
    }

    /**
     * retrieve the role from jwt token
     * @param userDetail userObject from authService
     * @return role
     */
    public String getRoleFromToken(UserDetail userDetail){
//        return getAllClaimsFromToken(token).get(ROLE, String.class);
        List<String> roles = userDetail.getRoles();
        return roles.isEmpty() ? "" : roles.get(0);
    }

    /**
     * retrieve expiration date from jwt token
     * @param token jwtToken
     * @return Date
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Get Claim From Token
     * @param token jwt
     * @param claimsResolver claimResolver
     * @param <T> Generic class
     * @return Generic class
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * for retrieving any information from token we will need the secret key
     * @param token jwt
     * @return Claim
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    /**
     * check if the token has expired
     * @param token jwt
     * @return boolean
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * validate token
     * @param token jwt from user
     * @param userDetails Spring Security UserDetail
     * @return true or false
     */
    public Boolean isValidToken(String token, UserDetails userDetails) {
//        final String username = getUsernameFromToken(token);
//        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        Optional<UserDetail> userDetailOptional = getUserDetail(token);
        if (userDetailOptional.isPresent()) {
            UserDetail userDetail = userDetailOptional.get();
            return String.valueOf(userDetail.getId()).equals(userDetails.getUsername());
        }
        return false;
    }

    public String generateToken(Date expiration) {
        return Jwts.builder()
                .setSubject("olutimedia@gmail.com")
                .claim(ROLE, "admin")
                .signWith(SignatureAlgorithm.HS256, secret)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiration)
                .compact();
    }

    /**
     * Generate Authentication
     * @param userDetails
     * @return
     */
    public UsernamePasswordAuthenticationToken getAuthentication(final UserDetails userDetails) {

        final Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

}
