package com.jitendra.paymentservice.config;

import com.jitendra.paymentservice.dto.GatewayResponse;
import com.jitendra.paymentservice.dto.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentGatewayClient {

    private final RestTemplate restTemplate;

    public PaymentGatewayClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GatewayResponse charge(PaymentRequest request) {

        String url = "https://payment-gateway/api/pay";

        return restTemplate.postForObject(
                url,
                request,
                GatewayResponse.class
        );
    }
}