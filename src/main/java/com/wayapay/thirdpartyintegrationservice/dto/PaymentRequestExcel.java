package com.wayapay.thirdpartyintegrationservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentRequestExcel {

    @ApiModelProperty(example = "Airtime")
    @NotBlank(message = "categoryId is required")
    private String categoryId;

    @ApiModelProperty(example = "mtnvtu")
    @NotBlank(message = "billerId is required")
    private String billerId;

    @ApiModelProperty(example = "79")
    @NotBlank(message = "source wallet account number is required")
    private String sourceWalletAccountNumber;

    @ApiModelProperty(example = "50")
    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "minimum amount required is 0.01")
    private Double amount;

    private String phone;

    private String paymentMethod;

    private String channel;

    private String userId;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getBillerId() {
        return billerId;
    }

    public void setBillerId(String billerId) {
        this.billerId = billerId;
    }

    public String getSourceWalletAccountNumber() {
        return sourceWalletAccountNumber;
    }

    public void setSourceWalletAccountNumber(String sourceWalletAccountNumber) {
        this.sourceWalletAccountNumber = sourceWalletAccountNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
