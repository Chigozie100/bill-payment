package com.wayapay.thirdpartyintegrationservice.util;

public class Constants {

    public static final String ERROR_MESSAGE = "Unable to process request";
    public static final String SUCCESS_MESSAGE = "Successful";
    public static final String API_V1 = "/api/v1";

    public static final String TOKEN_PREFIX = "serial ";
    public static final String HEADER_STRING = "Authorization";
    public static final String AUTHORITIES_KEY = "scopes";
    public static final String USERNAME = "username";
    public static final String ROLE = "role";
    public static final String TOKEN = "token";

    public static final String ID_IS_REQUIRED = "Id is required";
    public static final String ID_IS_UNKNOWN = "Unknown Id provided";
    public static final String ID_IS_INVALID = "Invalid Id provided";
    public static final String SYNCED_SUCCESSFULLY = "Synced successfully";
    public static final String SYNCED_IN_PROGRESS = "Syncing... it should take a long time";

    private Constants() {
    }
}
