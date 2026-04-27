package com.jitendra.paymentservice.service;

import com.jitendra.paymentservice.dto.PaymentRequest;
import com.jitendra.paymentservice.dto.PaymentResponse;
import com.jitendra.paymentservice.model.Payment;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequest request);

    PaymentResponse getPaymentStatus(Long orderId);
    Payment getPayment(Long orderId);

}