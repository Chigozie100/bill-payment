package com.wayapay.thirdpartyintegrationservice.elasticsearch;

import com.wayapay.thirdpartyintegrationservice.util.FinalStatus;
import com.wayapay.thirdpartyintegrationservice.util.Stage;
import com.wayapay.thirdpartyintegrationservice.util.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Document(indexName = "bills-payment-log")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    private String transactionId;
    private String transactionType; //{{billerId}}-{{categoryId}}
    private BigDecimal amount;
    private String userId;
    private String sourceAccountNumber;

    private String response;

    @Field(type = FieldType.Text)
    private Stage stage; //secureFund, contactVendorToProvideValue, logAsDispute

    @Field(type = FieldType.Text)
    private Status status; //log phrase - start/end

    @Field(type = FieldType.Text)
    private FinalStatus finalStatus; //Completed, Failed, Critical

    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt = new Date();
}
