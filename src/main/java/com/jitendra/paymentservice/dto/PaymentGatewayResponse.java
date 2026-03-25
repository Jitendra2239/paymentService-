package com.jitendra.paymentservice.dto;

import lombok.Data;

@Data
public class PaymentGatewayResponse {

    private String paymentLink;
    private String transactionId;
    private String status; // CREATED / SUCCESS / FAILED
    private  String razorpayOrderId;
}