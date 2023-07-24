package com.wayapay.thirdpartyintegrationservice.v2.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class WalletTransData {
    private Long id;
    private boolean del_flg;
    private boolean posted_flg;
    private String tranId;
    private String acctNum;
    private BigDecimal tranAmount;
    private String tranType;
    private String partTranType;
    private String tranNarrate;
    private LocalDate tranDate;
    private String tranCrncyCode;
    private String paymentReference;
    private String tranGL;
    private Integer tranPart;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long relatedTransId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updatedAt;
    private String tranCategory;
    private String createdBy;
    private String createdEmail;
    private String senderName;
    private String receiverName;
    private String transChannel;
    private boolean channel_flg;
    private BigDecimal chargeAmount;
    private BigDecimal vat;
}
