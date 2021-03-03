package com.wayapay.thirdpartyintegrationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * Security configuration
     * @param httpSecurity httpSecurity from spring
     * @throws Exception when there is an anomaly
     */
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        // We don't need CSRF for this example
        httpSecurity.cors().and().csrf().disable().authorizeRequests().anyRequest().permitAll();
                // dont authenticate this particular request
//                .authorizeRequests().antMatchers("/api/v1/login", "/api/v1/verify-token", "/",
//                "/api/v1/users/password/forgot","/api/v1/users/password/validate-recovery-code", "/api/v1/users/password/update",
//                "/v2/api-docs", "/swagger-ui.html", "/configuration/ui", "/swagger-resources/**", "/configuration/security",
//                "/webjars/**").permitAll().
                // all other requests need to be authenticated
//                        anyRequest().authenticated().and().
                // make sure we use stateless session; session won't be used to
                // store user's state.
//                        exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and().sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // Add a filter to validate the tokens with every request
//        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * TO avoid cross-origin
     * @return CorsConfigurationSource
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
