package com.wayapay.thirdpartyintegrationservice.service.interswitch;

import org.apache.commons.codec.binary.Base64;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.*;

public class RequestHeaders {

    public RequestHeaders() {
    }

    public Map<String, String> getISWAuthSecurityHeaders(String clientId, String clientSecretKey, String resourceUrl, String httpMethod) throws Exception {
        Map<String, String> interswitchAuth = new HashMap<>();
        long timestamp = generateTimestamp();
        String nonce = generateNonce();
        String signature = getSignature(clientId, clientSecretKey, resourceUrl, httpMethod, timestamp, nonce);
        String clientIdBase64 = new String(Base64.encodeBase64(clientId.getBytes()));
        String authorization = "InterswitchAuth " + clientIdBase64;
        interswitchAuth.put("AUTHORIZATION", authorization);
        interswitchAuth.put("TIMESTAMP", String.valueOf(timestamp));
        interswitchAuth.put("NONCE", nonce);
        interswitchAuth.put("SIGNATURE_METHOD", "SHA1");
        interswitchAuth.put("SIGNATURE", signature);
        return interswitchAuth;
    }

    private String getSignature(String clientId, String clientSecretKey, String resourceUrl, String httpMethod, long timestamp, String nonce) throws Exception {
        resourceUrl = URLEncoder.encode(resourceUrl, "ISO-8859-1");
        String signatureCipher = httpMethod + "&" + resourceUrl + "&" + timestamp + "&" + nonce + "&" + clientId + "&" + clientSecretKey;
        MessageDigest messagedigest = MessageDigest.getInstance("SHA1");  //SHA-512
        byte[] signaturebytes = messagedigest.digest(signatureCipher.getBytes());
        String signature = (new String(org.bouncycastle.util.encoders.Base64.encode(signaturebytes))).trim();
        return signature.replaceAll("\\s", "");
    }

    private long generateTimestamp() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Africa/Lagos"));
        return calendar.getTimeInMillis() / 1000L;
    }

    private String generateNonce() {
        UUID nonce = UUID.randomUUID();
        UUID nonce2 = UUID.randomUUID();
        String placeHolder1 = "-";
        String placeHolder2 = "";
        return nonce.toString().replaceAll(placeHolder1, placeHolder2)+nonce2.toString().replaceAll(placeHolder1, placeHolder2);
    }
}
