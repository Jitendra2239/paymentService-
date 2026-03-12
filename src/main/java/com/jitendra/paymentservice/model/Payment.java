package com.jitendra.paymentservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String paymentId;

    private String orderId;

    private Double amount;

    private String currency;

    private String paymentMethod;

    private String transactionId;

    private String status; // SUCCESS, FAILED, PENDING

    private LocalDateTime createdAt;

}