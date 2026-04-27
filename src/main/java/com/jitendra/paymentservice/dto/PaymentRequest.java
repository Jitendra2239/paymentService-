package com.jitendra.paymentservice.dto;

import lombok.Data;

@Data
public class PaymentRequest {

    private Long orderId;
    private Double amount;
    private String currency;
    private String paymentMethod;
    private String  name;
    private String  email;
    private Long userId;

}