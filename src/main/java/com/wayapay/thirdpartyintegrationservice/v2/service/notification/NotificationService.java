package com.wayapay.thirdpartyintegrationservice.v2.service.notification;

import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseObj;
import com.wayapay.thirdpartyintegrationservice.util.*;
import com.wayapay.thirdpartyintegrationservice.v2.dto.EventCategory;
import com.wayapay.thirdpartyintegrationservice.v2.entity.TransactionHistory;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;


@Slf4j
@RequiredArgsConstructor
@Service
@Data
public class NotificationService {
    private String smsMessage;
    private String emailMessage;

    private final NotificationFeignClient notificationFeignClient;

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
          ResponseEntity<ResponseObj<Object>>  responseEntity = notificationFeignClient.emailNotifyUserTransaction(emailEvent,token);

            log.info(String.format("userProfileResponse email sent status :: %s", Objects.requireNonNull(responseEntity.getBody()).status));
        } catch (Exception e) {
            log.error("Unable to generate transaction Id", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }

    }

    public void smsNotification(SmsEvent smsEvent, String token){
        try {
            ResponseEntity<ResponseObj<Object>>  responseEntity = notificationFeignClient.smsNotifyUserAtalking(smsEvent,token);
            log.info(" status :: " +Objects.requireNonNull(responseEntity.getBody()).status);
        } catch (Exception e) {
            log.info("::Error sending sms :: " +e.getMessage());
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


    public void pushSMSv2(TransactionHistory history, String token, String email,String phone){

        SmsEvent smsEvent = new SmsEvent();
        SmsPayload data = new SmsPayload();
        data.setMessage(formatMessageSMSv2(history.getAmount(),history.getCustomerDataToken(),history.getPaymentReferenceNumber(),history.getNarration()));

        data.setSmsEventStatus(SMSEventStatus.BILLSPAYMENT);
        PaymentTransactionDetailDto paymentTransactionDetailDto = new PaymentTransactionDetailDto();
        paymentTransactionDetailDto.setAmount(history.getAmount().doubleValue());
        paymentTransactionDetailDto.setTransactionId(history.getPaymentReferenceNumber());
        smsEvent.setPaymentTransactionDetail(paymentTransactionDetailDto);

        SmsRecipient smsRecipient = new SmsRecipient();
        smsRecipient.setEmail(email);
        smsRecipient.setTelephone(phone);
        List<SmsRecipient> addList = new ArrayList<>();
        addList.add(smsRecipient);

        data.setRecipients(addList);
        smsEvent.setData(data);

        smsEvent.setEventType(EventType.SMS.name());
        smsEvent.setInitiator(history.getSenderName());

        try {
            log.info(" smsEvent :: " + smsEvent);
            smsNotification(smsEvent,token);
        }catch (Exception ex){
            log.error("::Error smsEvent {}",ex.getLocalizedMessage());
        }
    }


    public String formatMessageSMSv2(BigDecimal amount, String code,String referenceNumber, String narration){
        String message= ""+"\n";
        message=message+"Amount :"+ amount +"\n";
        message=message+""+"Reference Code :"+ code +"\n";
        message=message+""+"Value :"+ referenceNumber +"\n";
        message=message+""+"Narration :"+  narration  +"\n";
        return message;
    }

}
