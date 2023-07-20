package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import com.wayapay.thirdpartyintegrationservice.v2.dto.BillCategoryName;
import com.wayapay.thirdpartyintegrationservice.v2.dto.PaymentStatus;
import com.wayapay.thirdpartyintegrationservice.v2.entity.*;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto.GeneralEpinData;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.List;

@Data
public class TransactionDto {
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal consumerFee = BigDecimal.ZERO;
    private BigDecimal billerFee = BigDecimal.ZERO;
    private String accountNumber;
    private String senderName;
    private String senderEmail;
    private String customerDataToken;
    private String narration;
    private String serviceProviderReferenceNumber;
    private ServiceProviderBiller serviceProviderBiller;
    private ServiceProviderProductBundle serviceProviderProductBundle;
    private ServiceProviderProduct serviceProviderProduct;
    private List<GeneralEpinData> epinData;
}
