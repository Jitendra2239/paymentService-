package com.jitendra.paymentservice.service;

import com.jitendra.paymentservice.dto.PaymentRequest;
import com.jitendra.paymentservice.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequest request);

    PaymentResponse getPaymentStatus(String orderId);

}