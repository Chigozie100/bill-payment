package com.wayapay.thirdpartyintegrationservice.responsehelper;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.SUCCESS_MESSAGE;

public class SuccessResponse extends ResponseHelper{

    public SuccessResponse(String message, Object data){
        super(true, message, data);
    }

    public SuccessResponse(Object data){
        super(true, SUCCESS_MESSAGE, data);
    }

}
