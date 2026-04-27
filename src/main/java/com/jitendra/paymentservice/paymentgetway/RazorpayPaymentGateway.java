package com.jitendra.paymentservice.paymentgetway;

import com.jitendra.paymentservice.dto.PaymentGatewayResponse;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.json.JSONObject;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.PaymentLink;

import java.util.Map;

@Component
public class RazorpayPaymentGateway implements IPaymentGateway {

    @Autowired
    private RazorpayClient razorpayClient;

    public PaymentGatewayResponse createPaymentLink(Double amount, Long orderId) {
        try {
           String email="";
           String phoneNumber="";
           String name="";
           System.out.println("emial from orderService:-"+email);
            int amountInPaise = (int) (amount * 100);
            long expireBy = (System.currentTimeMillis() / 1000) + (30 * 60);
            JSONObject paymentLinkRequest = new JSONObject();
            paymentLinkRequest.put("amount", 100);
            paymentLinkRequest.put("currency", "INR");
            paymentLinkRequest.put("accept_partial", true);
            paymentLinkRequest.put("first_min_partial_amount", 100);
            paymentLinkRequest.put("expire_by", expireBy);
            paymentLinkRequest.put("reference_id", String.valueOf(orderId));
            paymentLinkRequest.put("description", "Payment for policy no #23456");
            JSONObject customer = new JSONObject();
            customer.put("name", phoneNumber);
            customer.put("contact", name);
            customer.put("email", email);
            paymentLinkRequest.put("customer", customer);
            JSONObject notify = new JSONObject();
            notify.put("sms", true);
            notify.put("email", true);
            paymentLinkRequest.put("notify", notify);
            paymentLinkRequest.put("reminder_enable", true);
            JSONObject notes = new JSONObject();
            notes.put("orderId", orderId.toString());
            paymentLinkRequest.put("notes", notes);
            paymentLinkRequest.put("callback_url", "https://www.linkedin.com/in/jitendra-kumar-620a8715a/");
            paymentLinkRequest.put("callback_method", "get");

            PaymentLink payment = razorpayClient.paymentLink.create(paymentLinkRequest);
            System.out.println("payment->"+payment);
            PaymentGatewayResponse paymentGatewayResponse = new PaymentGatewayResponse();
            paymentGatewayResponse.setPaymentLink(payment.get("short_url"));
            paymentGatewayResponse.setTransactionId(payment.get("transaction_id"));
            JSONObject notesJson =(JSONObject) payment.get("notes"); // or just JSONObject notesJson = (JSONObject) payment.get("notes");


            String orderIdFromNotes = notesJson.getString("orderId");

            System.out.println("notes1->"+orderIdFromNotes);
            paymentGatewayResponse.setRazorpayOrderId(orderIdFromNotes );
          return  paymentGatewayResponse;
        }catch (RazorpayException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }
}