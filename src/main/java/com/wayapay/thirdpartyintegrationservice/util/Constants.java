package com.wayapay.thirdpartyintegrationservice.util;

public class Constants {

    public static final String ERROR_MESSAGE = "Unable to process request";
    public static final String BAXI_VALIDATATION = "Account Validation Error:Account does not exist in vending system";
    public static final String SUCCESS_MESSAGE = "Successful";
    public static final String API_V1 = "/api/v1";

    public static final String TOKEN_PREFIX = "serial ";
    public static final String HEADER_STRING = "Authorization";
    public static final String AUTHORITIES_KEY = "scopes";
    public static final String USERNAME = "username";
    public static final String ROLE = "role";
    public static final String TOKEN = "token";
    public static final String PIN = "pin";

    public static final String NGN = "NGN";
    public static final String LOCAL ="LOCAL";
    public static final String COMMISSION_PAYMENT_TRANSACTION= "COMMISSION-PAYMENT-TRANSACTION";
    public static final String COMMISSION = "COMMISSION"; 

    public static final String ID_IS_REQUIRED = "Id is required";
    public static final String ID_IS_UNKNOWN = "Unknown Id provided";
    public static final String ID_IS_INVALID = "Invalid Id provided";
    public static final String SYNCED_SUCCESSFULLY = "Synced successfully";
    public static final String SYNCED_IN_PROGRESS = "Syncing... it should take a long time";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String INAPP_TYPE = "IN-APP";
    public static final String INSUFFICIENT_FUND = "Insufficient fund";
    public static final String NOT_FOUND = "Id not found";
    public static final String ALREADY_RESOLVED = "Dispute already reversed";

    
    public static final String BAXI_INTRANSIT = "BAXI";
    public static final String VAT_BAXI_VAS_FEE_ACCOUNT = "VAT_BAXI";
    public static final String COLLECTION_BAXI_ACCOUNT = "INCOME_BAXI";
    public static final String DISBURS_BAXI = "DISBURS_BAXI";
    public static final String BAXI_SETTLEMENT_ACCOUNT = "BAXI_COMMISSION_SETTLEMENT";
    public static final String INCOME_BAXI = "INCOME_BAXI";
    public static final String BAXI_BILLS_PAYMENT_COMMISSION = "BAXI_BILLS_PAYMENT_COMMISSION";
    
    public static final String QUICKTELLER__INTRANSIT = "QUICKTELLER";
    public static final String VAT_QUICKTELLER_VAS_FEE_ACCOUNT = "VAT_QUICKTELLER";
    public static final String COMMISSION_QUICKTELLER_RECEIVABLE_ACCOUNT = "INCOME_QUICKTELLER";
    public static final String DISBURS_QUICKTELLER = "DISBURS_QUICKTELLER";
    public static final String QUICKTELLER_SETTLEMENT_ACCOUNT = "QUICKTELLER_COMMISSION_SETTLEMENT";
    public static final String QUICKTELLER_RECEIVABLE = "QUICKTELLER_RECEIVABLE";
    public static final String QUICKTELLER_COMMISSION_INTRANSIT = "QUICKTELLER";
    public static final String QUICKTELLER_BILLS_PAYMENT_COMMISSION = "QUICKTELLER_BILLS_PAYMENT_COMMISSION";

    

    public static final String INCOME_QUICKTELLER = "INCOME_QUICKTELLER";

    public static final String INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT = "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT";  // Done
    public static final String VAT_INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT = "VAT_INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT"; // Done
    public static final String INCOME_INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT = "INCOME_INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT"; // Done
    public static final String DISBURS_INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT = "DISBURS_INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT"; // Done
 




    private Constants() {
    }
}
