package com.jitendra.paymentservice.dto;

import lombok.Data;

@Data
public class GatewayResponse {

    private boolean success;

    private String transactionId;

    private String status;

    private String message;

    private String gatewayName;

    private String errorCode;

    public GatewayResponse() {
    }

    public GatewayResponse(boolean success, String transactionId, String status, String message) {
        this.success = success;
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}