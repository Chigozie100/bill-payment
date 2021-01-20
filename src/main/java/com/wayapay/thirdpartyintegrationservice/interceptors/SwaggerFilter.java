package com.wayapay.thirdpartyintegrationservice.interceptors;

import org.apache.logging.log4j.util.Strings;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SwaggerFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (Strings.EMPTY.equals(request.getRequestURI()) || "/".equals(request.getRequestURI())){
            response.sendRedirect("/swagger-ui.html");
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
