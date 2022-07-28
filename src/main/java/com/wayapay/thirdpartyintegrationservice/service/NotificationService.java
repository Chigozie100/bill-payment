package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.PaymentTransactionDetail;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseObj;
import com.wayapay.thirdpartyintegrationservice.service.notification.*;
import com.wayapay.thirdpartyintegrationservice.service.profile.ProfileService;
import com.wayapay.thirdpartyintegrationservice.service.profile.UserProfileResponse;
import com.wayapay.thirdpartyintegrationservice.util.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;


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
        inAppEvent.setToken(token);
        inAppEvent.setCategory(map.get("category"));

        return inAppEvent;
    }

    public void sendInAppNotification(InAppEvent inAppEvent, String token) throws ThirdPartyIntegrationException {
        try {
            ResponseEntity<ResponseObj<Object>> responseEntity = notificationFeignClient.inAppNotifyUser(inAppEvent,token);
            log.info(" status :: " +Objects.requireNonNull(responseEntity.getBody()).status);

        } catch (Exception e) {
            log.error("Unable to generate transaction Id", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }

    }
    public void sendEmailNotification(EmailEvent emailEvent, String token) throws ThirdPartyIntegrationException {

        try {
          //  ResponseEntity<ResponseObj> responseEntity = notificationFeignClient.emailNotifyUser(emailEvent,token);
            var responseEntity = notificationFeignClient.emailNotifyUserTransaction(emailEvent,token);

            log.info(String.format("userProfileResponse email sent status :: %s", Objects.requireNonNull(responseEntity.getBody()).status));
        } catch (Exception e) {
            log.error("Unable to generate transaction Id", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }

    }

    public void smsNotification(SmsEvent smsEvent, String token) throws ThirdPartyIntegrationException {
        try {
            ResponseEntity<ResponseObj<Object>>  responseEntity = notificationFeignClient.smsNotifyUserAtalking(smsEvent,token);
            log.info(" status :: " +Objects.requireNonNull(responseEntity.getBody()).status);
        } catch (Exception e) {
            log.info(" error sending sms :: " +e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }

    }

    public void pushINAPP2(String userId, PaymentTransactionDetail paymentTransactionDetail, PaymentResponse paymentResponse,  String token) throws ThirdPartyIntegrationException {
        // InAppEvent inAppEvent = buildInAppNotificationObject(map, token, EventType.IN_APP);

        InAppEvent appEvent = new InAppEvent();
        InAppPayload data = new InAppPayload();
        data.setMessage(formatMessageSMS(paymentResponse, paymentTransactionDetail));
        InAppRecipient appRecipient = new InAppRecipient();
        appRecipient.setUserId(Objects.requireNonNullElse(userId, "0"));

        List<InAppRecipient> addUserId = new ArrayList<>();
        addUserId.add(appRecipient);

        data.setUsers(addUserId);
        appEvent.setData(data);

        appEvent.setEventType("IN_APP");
        appEvent.setCategory("BILLS-PAYMENT");
        appEvent.setToken("token");
        appEvent.setInitiator(Objects.requireNonNullElse(userId, "0"));

        log.info("INAPP : " + appEvent);

        try {
            sendInAppNotification(appEvent, token);
        }catch (ThirdPartyIntegrationException ex){
            throw new ThirdPartyIntegrationException(HttpStatus.NOT_FOUND, ex.getMessage());
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

        emailEvent.setEventCategory(EventCategory.BILLS_PAYMENT);
        emailEvent.setProductType(ProductType.WAYABANK);
        emailEvent.setEventType(EventType.EMAIL.name());
        emailEvent.setNarration("Billspayment");
        emailEvent.setTransactionDate(new Date().toString());
        emailEvent.setTransactionId(map.get("transactionId"));
        emailEvent.setAmount(map.get("amount"));
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
        log.info("EMAIL ::: " + emailEvent);
        try {
            sendEmailNotification(emailEvent, token);
        }catch (ThirdPartyIntegrationException ex){
            throw new ThirdPartyIntegrationException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

    }

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
            log.info(" smsEvent :: " + smsEvent);
            smsNotification(smsEvent,token);

        }catch (ThirdPartyIntegrationException ex){
            throw new ThirdPartyIntegrationException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

    }


    public PaymentResponse buildRequest(PaymentResponse paymentResponse){
        List<ParamNameValue> valueList = paymentResponse.getData();
        for (int i = 0; i < valueList.size(); i++) {
            ParamNameValue value = new ParamNameValue();
            value.setName(valueList.get(i).getName());
            value.setValue(valueList.get(i).getValue());

            valueList.add(value);
        }
        paymentResponse.setData(valueList);

        return paymentResponse;
    }

    public String formatMessageSMS(PaymentResponse paymentResponse, PaymentTransactionDetail paymentTransactionDetail){
        String msg = null;
        String name = null;
        List<ParamNameValue> valueList = paymentResponse.getData();
        for (ParamNameValue paramNameValue : valueList) {
            ParamNameValue value = new ParamNameValue();
            value.setName(paramNameValue.getName());
            value.setValue(paramNameValue.getValue());
            name = value.getName();
//            msg = "Your account has " + "\n" +
//                    "" + "been credited with:" + paymentTransactionDetail.getAmount() + " \n" +
//                    "" + value.getValue();
        }

        String message= ""+"\n";
        message=message+"Amount :"+ paymentTransactionDetail.getAmount() +"\n";
        message=message+""+"Reference Code :"+paymentTransactionDetail.getTransactionId() +"\n";
        message=message+""+"Value :"+ msg +"\n";
        message=message+""+"Narration :"+  name  +"\n";

        return message;
    }

}
