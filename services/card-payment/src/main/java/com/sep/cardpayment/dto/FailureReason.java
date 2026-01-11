package com.sep.cardpayment.dto;

public enum FailureReason {
    INVALID_PAN,
    INVALID_EXPIRY_FORMAT,
    EXPIRED_CARD,
    INVALID_CVV,
    INVALID_HOLDER_NAME,
    CURRENCY_NOT_SUPPORTED
}
