package com.jitendra.paymentservice.controller;

import com.jitendra.paymentservice.dto.PaymentRequest;
import com.jitendra.paymentservice.dto.PaymentResponse;
import com.jitendra.paymentservice.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> pay(
            @RequestBody PaymentRequest request) {

        return ResponseEntity.ok(
                paymentService.processPayment(request)
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> status(
            @PathVariable String orderId) {

        return ResponseEntity.ok(
                paymentService.getPaymentStatus(orderId)
        );
    }
}