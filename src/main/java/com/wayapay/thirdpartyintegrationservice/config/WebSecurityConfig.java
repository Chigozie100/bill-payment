package com.wayapay.thirdpartyintegrationservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.API_V1;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationEntryPoint unauthorizedHandler;

    @Bean
    public JwtAuthenticationFilter authenticationTokenFilterBean() {
        return new JwtAuthenticationFilter();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    private static final String[] SWAGGER_WHITELIST = {
            // -- swagger ui
            "/swagger", "/v2/api-docs", "/swagger-resources", "/swagger-resources/**", "/configuration/ui", "/actuator/health",
            "/configuration/security", "/swagger-ui.html", "/webjars/**" };

    /**
     * Security configuration
     * @param httpSecurity httpSecurity from spring
     * @throws Exception when there is an anomaly
     */
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

//        httpSecurity.cors().and().csrf().disable().authorizeRequests().antMatchers("/**").permitAll();

        // We don't need CSRF for this example
        httpSecurity.cors().and().csrf().disable().authorizeRequests()
                .antMatchers("/","/actuator/**").permitAll()
                .antMatchers("/api/v1/commission/get-transactions-by-referralCode/{referralCode}").permitAll()
                .antMatchers("/api/v1/commission/update-transactions-status/{id}").permitAll()
                .antMatchers(SWAGGER_WHITELIST).permitAll()
                .antMatchers(HttpMethod.GET, API_V1+"/config/fee/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, API_V1+"/config/fee/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, API_V1+"/config/fee/**").hasRole("ADMIN")
//                .antMatchers(HttpMethod.GET, API_V1+"/config/**").hasRole("ADMIN")
                .anyRequest().authenticated().and().exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler).and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        httpSecurity.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
    }

}
