package com.wayapay.thirdpartyintegrationservice.interceptors;

import com.wayapay.thirdpartyintegrationservice.util.Constants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Map;

@Slf4j
public class FeignClientInterceptor implements RequestInterceptor {

	public static String getBearerTokenHeader() {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (attrs instanceof ServletRequestAttributes) {
			return ((ServletRequestAttributes) attrs).getRequest().getHeader(Constants.HEADER_STRING);
		}
		return null;
	}
	public static String getBearerClientIdHeader() {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (attrs instanceof ServletRequestAttributes) {
			return ((ServletRequestAttributes) attrs).getRequest().getHeader(Constants.CLIENT_ID);
		}
		return null;
	}
	public static String getBearerClientTypeHeader() {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (attrs instanceof ServletRequestAttributes) {
			return ((ServletRequestAttributes) attrs).getRequest().getHeader(Constants.CLIENT_TYPE);
		}
		return null;
	}


	@Override
	public void apply(RequestTemplate requestTemplate) {
		String token = getBearerTokenHeader();
		String clientId = getBearerClientIdHeader();
		String clientType = getBearerClientTypeHeader();

		Map<String, Collection<String>> headers = requestTemplate.request().headers();
		if(!ObjectUtils.isEmpty(token) && !headers.containsKey(Constants.HEADER_STRING)){
			requestTemplate.header(Constants.HEADER_STRING, token);
		}
		if(!headers.containsKey(Constants.CLIENT_ID)){
			requestTemplate.header(Constants.CLIENT_ID, clientId);
		}

		if(!headers.containsKey(Constants.CLIENT_TYPE)){
			requestTemplate.header(Constants.CLIENT_TYPE, clientType);
		}
	}

}
