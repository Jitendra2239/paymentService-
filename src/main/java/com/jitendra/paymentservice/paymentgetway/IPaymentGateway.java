package com.jitendra.paymentservice.paymentgetway;

import com.jitendra.paymentservice.dto.PaymentGatewayResponse;

public interface IPaymentGateway {

    PaymentGatewayResponse createPaymentLink(Double amount, Long orderId, String phoneNumber, String name, String email);
}
