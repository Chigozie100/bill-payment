package com.wayapay.thirdpartyintegrationservice.responsehelper;

import com.wayapay.thirdpartyintegrationservice.util.Constants;
import org.apache.logging.log4j.util.Strings;

public class ErrorResponse extends ResponseHelper {

    public ErrorResponse(String message){
        super(false, message, Strings.EMPTY);
    }

    public ErrorResponse(){
        super(false, Constants.ERROR_MESSAGE, Strings.EMPTY);
    }

}
