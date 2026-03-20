package com.jitendra.paymentservice.service;

import com.jitendra.paymentservice.dto.PaymentRequest;
import com.jitendra.paymentservice.dto.PaymentResponse;

public interface PaymentService {

    void processPayment(PaymentRequest request);

    PaymentResponse getPaymentStatus(Long orderId);

}