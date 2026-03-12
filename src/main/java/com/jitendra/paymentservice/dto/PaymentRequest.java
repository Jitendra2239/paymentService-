package com.jitendra.paymentservice.dto;

import lombok.Data;

@Data
public class PaymentRequest {

    private String orderId;
    private Double amount;
    private String currency;
    private String paymentMethod;

}