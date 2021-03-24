package com.wayapay.thirdpartyintegrationservice.config;

import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.auth.UserDetail;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private ProfileDetailsService userDetailsService;

    @Autowired
    private JwtTokenHelper jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {

        String authToken = getAuthToken(req.getHeader(Constants.HEADER_STRING)).orElse(null);
        String username = getUsername(authToken).orElse(null);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.info("Wha are you doing here name {} context {} ",username,SecurityContextHolder.getContext().getAuthentication());
            UserDetails userDetails = userDetailsService.loadUserByUsername(authToken);

            if (Boolean.TRUE.equals(jwtTokenUtil.isValidToken(authToken, userDetails))) {
                UsernamePasswordAuthenticationToken authentication = jwtTokenUtil.getAuthentication(userDetails);
                logger.info("authenticated user " + username + ", setting security context");
                SecurityContextHolder.getContext().setAuthentication(authentication);
                req.setAttribute(Constants.USERNAME, username);
            }
        }

        chain.doFilter(req, res);
    }

    private Optional<String> getAuthToken(String header){
        log.info("Token is {}", jwtTokenUtil.generateToken(CommonUtils.convertToDate(LocalDate.now().plusDays(30))));
        if (!Objects.isNull(header) && header.startsWith(Constants.TOKEN_PREFIX)) {
//            String[] authTokenArray = header.split("\\s+");
//            return Optional.of(authTokenArray.length == 2 ? authTokenArray[1] : authTokenArray[0]);
            return Optional.of(header);
        }
        return Optional.ofNullable(header);
    }

    private Optional<String> getUsername(String authToken){

        if (!Objects.isNull(authToken)){
            try {
                Optional<UserDetail> userDetailOptional = jwtTokenUtil.getUserDetail(authToken);
                return userDetailOptional.map(userDetail -> jwtTokenUtil.getUsernameFromToken(userDetail));
            } catch (IllegalArgumentException e) {
                log.error("an error occurred during getting username fromUser token", e);
            } catch (ExpiredJwtException e) {
                log.warn("the token is expired and not valid anymore "+ e.getMessage());
            } catch (SignatureException e) {
                log.warn("Authentication Failed. Username or Password not valid. "+e.getMessage());
            } catch (MalformedJwtException e) {
                log.warn("Malformed token."+e.getMessage());
            }
        }

        return Optional.empty();
    }
}