package com.wayapay.thirdpartyintegrationservice.sample.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SamplePaymentItemsResponse {

    @ApiModelProperty(notes = "timeStamp", example = "2021-05-16 11:28:45")
    private String timeStamp;

    @ApiModelProperty(notes = "status", example = "true")
    private String status;

    @ApiModelProperty(notes = "message", example = "Successful")
    private String message;

    @ApiModelProperty(notes = "data", example = "{\n" +
            "    \"categoryId\": \"Airtime\",\n" +
            "    \"billerId\": \"mtnvtu\",\n" +
            "    \"items\": [\n" +
            "      {\n" +
            "        \"paramName\": \"phone\",\n" +
            "        \"isAmountFixed\": false,\n" +
            "        \"subItems\": []\n" +
            "      },\n" +
            "      {\n" +
            "        \"paramName\": \"amount\",\n" +
            "        \"isAmountFixed\": false,\n" +
            "        \"subItems\": []\n" +
            "      },\n" +
            "      {\n" +
            "        \"paramName\": \"paymentMethod\",\n" +
            "        \"isAmountFixed\": false,\n" +
            "        \"subItems\": [\n" +
            "          {\n" +
            "            \"id\": \"cash\",\n" +
            "            \"name\": \"cash\",\n" +
            "            \"minAmount\": \"0\",\n" +
            "            \"amount\": null\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"paramName\": \"channel\",\n" +
            "        \"isAmountFixed\": false,\n" +
            "        \"subItems\": [\n" +
            "          {\n" +
            "            \"id\": \"ATM\",\n" +
            "            \"name\": \"ATM\",\n" +
            "            \"minAmount\": \"0\",\n" +
            "            \"amount\": null\n" +
            "          },\n" +
            "          {\n" +
            "            \"id\": \"B2B\",\n" +
            "            \"name\": \"B2B\",\n" +
            "            \"minAmount\": \"0\",\n" +
            "            \"amount\": null\n" +
            "          },\n" +
            "          {\n" +
            "            \"id\": \"WEB\",\n" +
            "            \"name\": \"WEB\",\n" +
            "            \"minAmount\": \"0\",\n" +
            "            \"amount\": null\n" +
            "          },\n" +
            "          {\n" +
            "            \"id\": \"MOBILE\",\n" +
            "            \"name\": \"MOBILE\",\n" +
            "            \"minAmount\": \"0\",\n" +
            "            \"amount\": null\n" +
            "          },\n" +
            "          {\n" +
            "            \"id\": \"ANDROIDPOS\",\n" +
            "            \"name\": \"ANDROIDPOS\",\n" +
            "            \"minAmount\": \"0\",\n" +
            "            \"amount\": null\n" +
            "          },\n" +
            "          {\n" +
            "            \"id\": \"LINUXPOS\",\n" +
            "            \"name\": \"LINUXPOS\",\n" +
            "            \"minAmount\": \"0\",\n" +
            "            \"amount\": null\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ],\n" +
            "    \"isValidationRequired\": false\n" +
            "  }")
    private String data;
}
