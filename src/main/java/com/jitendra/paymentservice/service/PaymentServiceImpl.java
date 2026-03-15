package com.jitendra.paymentservice.service;

import com.jitendra.event.InventoryReservedEvent;
import com.jitendra.event.PaymentFailedEvent;
import com.jitendra.event.PaymentSuccessEvent;
import com.jitendra.paymentservice.config.PaymentGatewayClient;
import com.jitendra.paymentservice.dto.GatewayResponse;
import com.jitendra.paymentservice.dto.PaymentRequest;
import com.jitendra.paymentservice.dto.PaymentResponse;

import com.jitendra.paymentservice.model.Payment;
import com.jitendra.paymentservice.repository.PaymentRepository;
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
    @KafkaListener(topics = "inventory-reserved", groupId = "payment-group")
    public void consumeInventoryReserved(InventoryReservedEvent event) {

        System.out.println("Processing payment for Order: " + event.getOrderId());
        PaymentRequest request =new PaymentRequest();
        PaymentResponse paymentResponse  = new PaymentResponse();
      paymentResponse.setStatus("FAILED");
      paymentResponse.setPaymentId("payment-id");
      paymentResponse.setMessage("message");
        if( paymentResponse.getStatus().equals("SUCCESS") ) {
            PaymentSuccessEvent event1 =new PaymentSuccessEvent();
            event1.setOrderId(event.getOrderId());
            event1.setPaymentId(paymentResponse.getPaymentId());

            kafkaTemplate.send("payment-success", event1);
        } else {
            PaymentFailedEvent event1 =new PaymentFailedEvent();
            event1.setOrderId(event.getOrderId());
            event1.setPaymentId(paymentResponse.getPaymentId());
            kafkaTemplate.send("payment-failed", event1);
        }
    }
//    @KafkaListener(topics = "order-created")
//    public void processPayment(OrderCreatedEvent event){
//
//        System.out.println("Processing payment");
//
//        PaymentSuccessEvent paymentEvent =
//                new PaymentSuccessEvent(event.getOrderId(),"PAY123");
//
//        kafkaTemplate.send("payment-success", paymentEvent);
//
//    }
}