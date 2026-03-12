package com.jitendra.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {

    private String paymentId;
    private String status;
    private String transactionId;
    private String message;

}