//package com.wayapay.thirdpartyintegrationservice.interceptors;
//
//import com.wayapay.thirdpartyintegrationservice.util.Constants;
//import feign.RequestInterceptor;
//import feign.RequestTemplate;
//import org.apache.http.entity.ContentType;
//import org.springframework.http.HttpHeaders;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestAttributes;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//@Component
//public class FeignClientInterceptor implements RequestInterceptor {
//
//    private static final String AUTHORIZATION_HEADER = "Authorization";
//
//    public static String getBearerTokenHeader() {
//        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
//        if (attrs instanceof ServletRequestAttributes) {
//            return ((ServletRequestAttributes) attrs).getRequest().getHeader("Authorization");
//        }
//        return null;
//    }
//
//    public static String getBearerClientIdHeader() {
//        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
//        if (attrs instanceof ServletRequestAttributes) {
//            return ((ServletRequestAttributes) attrs).getRequest().getHeader(Constants.CLIENT_ID);
//        }
//        return null;
//    }
//
//    public static String getBearerClientTypeHeader() {
//        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
//        if (attrs instanceof ServletRequestAttributes) {
//            return ((ServletRequestAttributes) attrs).getRequest().getHeader(Constants.CLIENT_TYPE);
//        }
//        return null;
//    }
//
//    @Override
//    public void apply(RequestTemplate requestTemplate) {
//        String token = getBearerTokenHeader();
//        if(token != null && !token.isBlank())
//            requestTemplate.header(AUTHORIZATION_HEADER, token);
//
//        String clientId = getBearerClientIdHeader();
//        if(clientId != null && !clientId.isBlank()){
//            requestTemplate.header(Constants.CLIENT_ID, clientId);
//        }else {
//            requestTemplate.header(Constants.CLIENT_ID, Constants.CLIENT_ID_VALUE);
//        }
//
//
//        String clientType = getBearerClientTypeHeader();
//        if (clientType != null && !clientType.isBlank()){
//            requestTemplate.header(Constants.CLIENT_TYPE, clientType);
//        }else {
//            requestTemplate.header(Constants.CLIENT_TYPE, Constants.CLIENT_TYPE_VALUE_CORPORATE);
//        }
//
//         requestTemplate.header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
//    }
//
//}