package com.jitendra.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private String paymentId;
    private String status;
    private String transactionId;
    private String message;

}