package com.wayapay.thirdpartyintegrationservice.service.dispute;

import com.wayapay.thirdpartyintegrationservice.annotations.AuditPaymentOperation;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import com.wayapay.thirdpartyintegrationservice.util.Stage;
import com.wayapay.thirdpartyintegrationservice.util.Status;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class DisputeService {

    private final DisputeServiceFeignClient disputeServiceFeignClient;

    @Async
    @AuditPaymentOperation(stage = Stage.LOG_AS_DISPUTE, status = Status.END)
    public boolean logTransactionAsDispute(String username, Object request, ThirdPartyNames thirdPartyName,
                                        String billerId, String categoryId, BigDecimal amount, String transactionId){
        DisputeRequest disputeRequest = new DisputeRequest();
        disputeRequest.setUserId(username);
        disputeRequest.setExtraDetails(CommonUtils.objectToJson(request).orElse(""));
        disputeRequest.setNarrationOfDispute("BIllsPayment via "+thirdPartyName+" for "+billerId+" in the category called "+categoryId);
        disputeRequest.setTransactionAmount(amount.toPlainString());
        disputeRequest.setTransactionDate(CommonUtils.getDateAsString(LocalDateTime.now()));
        disputeRequest.setTransactionId(transactionId);
        try {
            DisputeResponse disputeResponse = disputeServiceFeignClient.logTransactionAsDispute(disputeRequest);
            log.info("Response from dispute service -> {}", disputeResponse);
            return true;
        } catch (Exception e) {
            log.error("Unable log transaction for dispute ", e);
            return false;
        }
    }

}
