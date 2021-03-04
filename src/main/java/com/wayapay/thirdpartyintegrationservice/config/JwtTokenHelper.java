package com.wayapay.thirdpartyintegrationservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.ROLE;

@Slf4j
@Component
public class JwtTokenHelper implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    @Value("${jwt.secret}")
    private String secret;

    /**
     * retrieve username from jwt token
     * @param token jwtToken
     * @return subject
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * retrieve the role from jwt token
     * @param token jwt token
     * @return role
     */
    public String getRoleFromToken(String token){
        return getAllClaimsFromToken(token).get(ROLE, String.class);
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
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String generateToken(Date expiration) {
        return Jwts.builder()
                .setSubject("olutimedia@gmail.com")
                .claim(ROLE, "user")
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
