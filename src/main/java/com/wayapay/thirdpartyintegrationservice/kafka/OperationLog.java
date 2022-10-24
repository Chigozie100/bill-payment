package com.wayapay.thirdpartyintegrationservice.kafka;

import com.wayapay.thirdpartyintegrationservice.util.FinalStatus;
import com.wayapay.thirdpartyintegrationservice.util.Stage;
import com.wayapay.thirdpartyintegrationservice.util.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class OperationLog {

    private String id;
    private String transactionId;
    private String transactionType;
    private String logType;
    private BigDecimal amount;
    private String userId;
    private String sourceAccountNumber;
    private String response;
    private Stage stage; //secureFund, contactVendorToProvideValue, logAsDispute
    private Status status; //log phrase - start/end
    private FinalStatus finalStatus; //Completed, Failed, Critical
    private Date createdAt = new Date();
    private String category;
}
