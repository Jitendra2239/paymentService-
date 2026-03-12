package com.jitendra.paymentservice.service;

import com.jitendra.paymentservice.config.PaymentGatewayClient;
import com.jitendra.paymentservice.dto.GatewayResponse;
import com.jitendra.paymentservice.dto.PaymentRequest;
import com.jitendra.paymentservice.dto.PaymentResponse;
import com.jitendra.paymentservice.model.Payment;
import com.jitendra.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayClient gatewayClient;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              PaymentGatewayClient gatewayClient) {
        this.paymentRepository = paymentRepository;
        this.gatewayClient = gatewayClient;
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {

        Optional<Payment> existingPayment =
                paymentRepository.findByOrderId(request.getOrderId());

        if(existingPayment.isPresent()) {
            throw new RuntimeException("Payment already processed");
        }

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus("PENDING");
        payment.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        GatewayResponse gatewayResponse =
                gatewayClient.charge(request);

        if(gatewayResponse.isSuccess()) {

            payment.setStatus("SUCCESS");
            payment.setTransactionId(gatewayResponse.getTransactionId());

        } else {

            payment.setStatus("FAILED");

        }

        paymentRepository.save(payment);

        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getStatus(),
                payment.getTransactionId(),
                "Payment processed"
        );
    }

    @Override
    public PaymentResponse getPaymentStatus(String orderId) {

        Payment payment = paymentRepository
                .findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getStatus(),
                payment.getTransactionId(),
                "Payment status fetched"
        );
    }
}