package com.jitendra.paymentservice.controller;

import com.jitendra.event.PaymentFailedEvent;
import com.jitendra.event.PaymentSuccessEvent;
import com.jitendra.paymentservice.model.Payment;
import com.jitendra.paymentservice.model.PaymentStatus;
import com.jitendra.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import static com.razorpay.Utils.verifySignature;

@RestController
@RequestMapping("api/v1//payment")
public class PaymentWebhookController {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public PaymentWebhookController(PaymentRepository paymentRepository,
                                    KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }
    private  String hmacSHA256(String data, String secret) throws Exception {

        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec secretKey =
                new javax.crypto.spec.SecretKeySpec(secret.getBytes(), "HmacSHA256");

        mac.init(secretKey);

        byte[] rawHmac = mac.doFinal(data.getBytes());

        StringBuilder hex = new StringBuilder();
        for (byte b : rawHmac) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) hex.append('0');
            hex.append(s);
        }
        return hex.toString();
    }
    private boolean verifySignature(String payload, String signature) {
        try {
            String generatedSignature = hmacSHA256(payload, webhookSecret);
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        System.out.println("Webhook received: " + payload);
        if (!verifySignature(payload, signature)) {
            return ResponseEntity.status(400).body("Invalid signature");
        }
        JSONObject json = new JSONObject(payload);

        String eventType = json.getString("event");

        JSONObject paymentEntity = json
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String razorpayPaymentId = paymentEntity.getString("id");
        String razorpayOrderId = paymentEntity.getString("order_id");
        String status = paymentEntity.getString("status");

        Payment payment = paymentRepository
                .findByTransactionId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (PaymentStatus.SUCCESS.equals(payment.getStatus())) {
            return ResponseEntity.ok("Already processed");
        }
        if ("payment.captured".equals(eventType)) {

            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(razorpayPaymentId);

            paymentRepository.save(payment);

            // 🔥 Kafka event
            PaymentSuccessEvent event = new PaymentSuccessEvent();
            event.setOrderId(payment.getOrderId());
            event.setPaymentId(payment.getPaymentId());
            event.setName(payment.getName());
            event.setEmail(payment.getEmail());
            event.setPhone(payment.getPhoneNumber());

            kafkaTemplate.send("payment-success", event);
        }
        else if ("payment.failed".equals(eventType)) {

            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
          PaymentFailedEvent  event= new PaymentFailedEvent();
            event.setOrderId(payment.getOrderId());
            event.setPaymentId(payment.getPaymentId());
            event.setName(payment.getName());
            event.setEmail(payment.getEmail());
            event.setPhone(payment.getPhoneNumber());
            kafkaTemplate.send("payment-failed", event);
        }
        return ResponseEntity.ok("Webhook processed");
    }
    }





