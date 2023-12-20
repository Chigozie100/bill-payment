package com.wayapay.thirdpartyintegrationservice.interceptors;

import com.wayapay.thirdpartyintegrationservice.util.Constants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

		if(token != null && !token.isEmpty())
			requestTemplate.header(Constants.HEADER_STRING, token);

		if(clientId != null && !clientId.isEmpty())
			requestTemplate.header(Constants.CLIENT_ID, clientId);

		if(clientType != null && !clientType.isEmpty())
			requestTemplate.header(Constants.CLIENT_TYPE, clientType);

		requestTemplate.header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
	}

}
