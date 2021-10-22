package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.PaymentTransactionDetail;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseObj;
import com.wayapay.thirdpartyintegrationservice.service.notification.*;
import com.wayapay.thirdpartyintegrationservice.service.profile.ProfileService;
import com.wayapay.thirdpartyintegrationservice.service.profile.UserProfileResponse;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import com.wayapay.thirdpartyintegrationservice.util.EventType;
import com.wayapay.thirdpartyintegrationservice.util.SMSEventStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Slf4j
@RequiredArgsConstructor
@Service
@Data
public class NotificationService {
    private String smsMessage;
    private String emailMessage;

    private final NotificationFeignClient notificationFeignClient;
    private final ProfileService profileService;

    private InAppEvent buildInAppNotificationObject(Map<String, String> map, String token, EventType eventType){
        InAppEvent inAppEvent = new InAppEvent();
        inAppEvent.setEventType(eventType.name());

        InAppPayload data = new InAppPayload();
        data.setMessage(map.get("message"));
        data.setType(eventType.name());

        InAppRecipient inAppRecipient = new InAppRecipient();
        inAppRecipient.setUserId(map.get("userId"));

        List<InAppRecipient> addUserId = new ArrayList<>();
        addUserId.add(inAppRecipient);

        data.setUsers(addUserId);

        inAppEvent.setData(data);
        inAppEvent.setInitiator(map.get("userId"));

        return inAppEvent;
    }


    public NotificationDto buildInAppNotificationObject(Map<String, Object> map, String token, EventType eventType, PaymentResponse paymentResponse, String data){
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setMessage(map.get("message").toString());
        notificationDto.setType(eventType.name());
        notificationDto.setUserId(map.get("userId").toString());
        notificationDto.setEventType(eventType.name());
        notificationDto.setInitiator(map.get("userId").toString());
        return notificationDto;
    }

    public void sendInAppNotification(InAppEvent inAppEvent, String token) throws ThirdPartyIntegrationException {
        try {
            ResponseEntity<ResponseObj> responseEntity = notificationFeignClient.inAppNotifyUser(inAppEvent,token);
            ResponseObj infoResponse = responseEntity.getBody();
            log.info("Bills-payment InApp response :: " +infoResponse.data);
        } catch (Exception e) {
            log.error("Unable to generate transaction Id", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }

    }
    public void sendEmailNotification(EmailEvent emailEvent, String token) throws ThirdPartyIntegrationException {

        try {
            ResponseEntity<ResponseObj> responseEntity = notificationFeignClient.emailNotifyUser(emailEvent,token);
            ResponseObj infoResponse = responseEntity.getBody();
            log.info("userProfileResponse email sent status :: " +infoResponse.status);
        } catch (Exception e) {
            log.error("Unable to generate transaction Id", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }

    }

    public Boolean smsNotification(SmsEvent smsEvent, String token) throws ThirdPartyIntegrationException {
        try {
            log.info("about to send SMS :: " +smsEvent);
            ResponseEntity<ResponseObj>  responseEntity = notificationFeignClient.smsNotifyUser(smsEvent,token);
            ResponseObj infoResponse = responseEntity.getBody();
            log.info("userProfileResponse sms sent status :: " +infoResponse.status);
            return infoResponse.status;
        } catch (Exception e) {
            log.error("Unable to send SMS", e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }

    }


    public void pushINAPP(Map<String, String> map, String token) throws ThirdPartyIntegrationException {
        InAppEvent inAppEvent = buildInAppNotificationObject(map, token, EventType.IN_APP);
        try {
            sendInAppNotification(inAppEvent, token);
        }catch (ThirdPartyIntegrationException ex){
            throw new ThirdPartyIntegrationException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

    }


    public void pushEMAIL(Map<String, String> map, String token) throws ThirdPartyIntegrationException {

        EmailEvent emailEvent = new EmailEvent();

        emailEvent.setEventType(EventType.EMAIL.name());
        EmailPayload data = new EmailPayload();

        data.setMessage(map.get("message"));

        EmailRecipient emailRecipient = new EmailRecipient();
        emailRecipient.setFullName(map.get("surname") + " " +map.get("firstName") + " " + map.get("middleName"));
        emailRecipient.setEmail(map.get("email"));

        List<EmailRecipient> addUserId = new ArrayList<>();
        addUserId.add(emailRecipient);
        data.setNames(addUserId);

        emailEvent.setData(data);
        emailEvent.setInitiator(map.get("userId"));

        try {
            sendEmailNotification(emailEvent, token);
        }catch (ThirdPartyIntegrationException ex){
            throw new ThirdPartyIntegrationException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

    }

//    public boolean pushSMS(PaymentTransactionDetail paymentTransactionDetail, String token, PaymentResponse paymentResponse) throws ThirdPartyIntegrationException {
//        SMSDto smsDto = buildSMSObject(paymentTransactionDetail, token, EventType.SMS, paymentResponse);
//        try {
//            log.info("Still on the mission " + smsDto);
//            log.info("Still on the mission " + token);
//            // check if User enables SMS charge
//            smsNotification(smsDto,token);
//
//        }catch (ThirdPartyIntegrationException ex){
//            throw new ThirdPartyIntegrationException(HttpStatus.NOT_FOUND, ex.getMessage());
//        }
//        return false;
//    }

    public void pushSMS(PaymentTransactionDetail paymentTransactionDetail, String token, PaymentResponse paymentResponse, UserProfileResponse userProfileResponse) throws ThirdPartyIntegrationException {

        SmsEvent smsEvent = new SmsEvent();
        SmsPayload data = new SmsPayload();
        data.setMessage(formatMessageSMS(paymentResponse, paymentTransactionDetail));
        smsEvent.setPaymentResponse(paymentResponse);

        data.setSmsEventStatus(SMSEventStatus.BILLSPAYMENT);

        PaymentTransactionDetailDto paymentTransactionDetailDto = new PaymentTransactionDetailDto();
        paymentTransactionDetailDto.setAmount(paymentTransactionDetail.getAmount().doubleValue());
        paymentTransactionDetailDto.setTransactionId(paymentTransactionDetail.getTransactionId());
        smsEvent.setPaymentTransactionDetail(paymentTransactionDetailDto);


        SmsRecipient smsRecipient = new SmsRecipient();
        smsRecipient.setEmail(userProfileResponse.getEmail());
        smsRecipient.setTelephone(userProfileResponse.getPhoneNumber());
        List<SmsRecipient> addList = new ArrayList<>();
        addList.add(smsRecipient);

        data.setRecipients(addList);
        smsEvent.setData(data);

        smsEvent.setEventType(EventType.SMS.name());
        smsEvent.setInitiator(paymentTransactionDetail.getUsername());

        try {
            log.info("smsEvent ::: " + smsEvent);

            smsNotification(smsEvent,token);

        }catch (ThirdPartyIntegrationException ex){
            throw new ThirdPartyIntegrationException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

    }

        public String formatMessage(Map<String, String> dto){
        String message= ""+"\n";
        message=message+"Amount :"+ "dto " +"\n";
        message=message+"\n"+"Reference Code :"+"dto "+"\n";
        message=message+"\n"+"Sender Name :"+"dto "+"\n";
        message=message+"\n"+"Receiver Name :"+"dto "+"\n";
        message=message+"\n"+"Phone Number :"+"dto "+"\n";
        message=message+"\n"+"Narration :"+"dto "+"  "+"\n";
        return message;
    }

    public PaymentResponse buildRequest(PaymentResponse paymentResponse){
       // ParamNameValue paramNameValue = new ParamNameValue();
        List<ParamNameValue> valueList = paymentResponse.getData();
        for (int i = 0; i < valueList.size(); i++) {
            ParamNameValue value = new ParamNameValue();
            value.setName(valueList.get(i).getName());
            value.setValue(valueList.get(i).getValue());

//            paramNameValue.setValue(valueList.get(i).getValue());
//            paramNameValue.setName(valueList.get(i).getName());
            valueList.add(value);
        }
        paymentResponse.setData(valueList);

        return paymentResponse;
    }

    public String formatMessageSMS(PaymentResponse paymentResponse, PaymentTransactionDetail paymentTransactionDetail){
        String msg = null;
        String name = null;
        List<ParamNameValue> valueList = paymentResponse.getData();
        for (int i = 0; i < valueList.size(); i++) {
            ParamNameValue value = new ParamNameValue();
            value.setName(valueList.get(i).getName());
            value.setValue(valueList.get(i).getValue());
            name = value.getName();
            msg = "Your account has "+ "\n" +
                    ""+"been credited with:" + paymentTransactionDetail.getAmount() +" \n" +
                    "" + value.getValue();
        }

        String message= ""+"\n";
        message=message+"Amount :"+ paymentTransactionDetail.getAmount() +"\n";
        message=message+""+"Reference Code :"+paymentTransactionDetail.getTransactionId() +"\n";
        message=message+""+"Value :"+ msg +"\n";
        message=message+""+"Narration :"+  name  +"\n";

        return message;
    }


    public String getMessageDetails(Map<String, Object> map){
        PaymentResponse data = new PaymentResponse();
//        map.put("paymentTransactionAmount", paymentTransactionDetail.getAmount());
//        map.put("paramNameValueList",paymentResponse.getData());
        List<ParamNameValue> valueList = (List<ParamNameValue>) map.get("paramNameValueList");
        //paymentResponse.getData();
        String message = null;
        for (int i = 0; i < valueList.size(); i++) {
            ParamNameValue value = new ParamNameValue();
            value.setName(valueList.get(i).getName());
            value.setValue(valueList.get(i).getValue());
            message = "Your account has "+ "\n" +
                    ""+"been credited with:" + map.get("paymentTransactionAmount") +" \n" +
                    "" + value.getValue();
//            message = "name :" + value.getName() +"  \"<br>\"" +
//            " \n" +  "Value : " + value.getValue();
        }
        return message;

    }
    public String getMessageServiceCharge(PaymentResponse paymentResponse, PaymentTransactionDetail paymentTransactionDetail){
        PaymentResponse data = new PaymentResponse();
        List<ParamNameValue> valueList = paymentResponse.getData();
        String message = null;
        for (int i = 0; i < valueList.size(); i++) {
            ParamNameValue value = new ParamNameValue();
            value.setName(valueList.get(i).getName());
            value.setValue(valueList.get(i).getValue());
            message = paymentTransactionDetail.getAmount() + "has been deducted from "+ "\n" +
                    ""+"your account for SMS Charges:";

        }
        return message;

    }
}
