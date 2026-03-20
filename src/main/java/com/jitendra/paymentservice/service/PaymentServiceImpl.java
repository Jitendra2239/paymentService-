package com.jitendra.paymentservice.service;

import com.jitendra.event.*;
import com.jitendra.paymentservice.config.PaymentGatewayClient;
import com.jitendra.paymentservice.dto.GatewayResponse;
import com.jitendra.paymentservice.dto.PaymentGatewayResponse;
import com.jitendra.paymentservice.dto.PaymentRequest;
import com.jitendra.paymentservice.dto.PaymentResponse;

import com.jitendra.paymentservice.exception.PaymentNotFoundException;
import com.jitendra.paymentservice.model.Payment;
import com.jitendra.paymentservice.model.PaymentStatus;
import com.jitendra.paymentservice.paymentgetway.IPaymentGateway;
import com.jitendra.paymentservice.paymentgetway.PaymentGatewayChooserStrategy;
import com.jitendra.paymentservice.repository.PaymentRepository;
import com.razorpay.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayClient gatewayClient;
    private final KafkaTemplate<String,Object> kafkaTemplate;

    private  final PaymentGatewayChooserStrategy strategy;

    public PaymentGatewayResponse getPaymentLink(Double amount,Long orderId,String phoneNumber,
                                 String name, String email) {
        IPaymentGateway paymentGateway = strategy.getBestPaymentGateway();
        return paymentGateway.createPaymentLink(amount, orderId, phoneNumber,
                name, email);
    }
    @Override
    public void processPayment(PaymentRequest request) {

        Optional<Payment> existingPayment =
                paymentRepository.findByOrderId(request.getOrderId());

        if (existingPayment.isPresent()) {
            throw new RuntimeException("Payment already processed");
        }

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setEmail(request.getEmail());
        payment.setName(request.getName());
        payment.setPhoneNumber(request.getPhone());
        payment.setCreatedAt(LocalDateTime.now());


        payment.setStatus(PaymentStatus.PENDING);

        paymentRepository.save(payment);


        PaymentGatewayResponse response =
                strategy.getBestPaymentGateway()
                        .createPaymentLink(payment.getAmount(), payment.getOrderId(), payment.getPhoneNumber(), payment.getEmail(), payment.getName());


        payment.setTransactionId(response.getTransactionId());
        paymentRepository.save(payment);




    }

    @Override
    public PaymentResponse getPaymentStatus(Long orderId) {

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
    @KafkaListener(topics = "inventory-reserved", groupId = "payment-group")
    public void consumeInventoryReserved(InventoryReservedEvent event) {

        System.out.println("Processing payment for Order: " + event.getOrderId());
        PaymentRequest request =new PaymentRequest();
             request.setAmount(event.getAmount());
             request.setEmail(event.getEmail());
             request.setCurrency(event.getCurrency());
             request.setName(event.getFirstName());
             request.setPhone(event.getPhone());

        processPayment(request);


    }

    public void handlePaymentSuccess(Long orderId, String transactionId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(transactionId);

        paymentRepository.save(payment);

        // 🔥 Notify OrderService
        kafkaTemplate.send("payment-success",
                new PaymentSuccessEvent(orderId));

        // 🔔 Notification
        NotificationEvent event = new NotificationEvent();
        event.setType("PAYMENT_SUCCESS");
        event.setMessage("Payment successful!");

        kafkaTemplate.send("notification-topic", event);
    }

    @KafkaListener(topics = "order-cancelled")
    public void refund(OrderCancelledEvent event) {
        Payment payment = paymentRepository.findByOrderId(event.getOrderId()).orElseThrow(() -> new PaymentNotFoundException("Payment not Found for OrderId" + event.getOrderId()));

        payment.setStatus("REFUNDED");
        paymentRepository.save(payment);
    }


}