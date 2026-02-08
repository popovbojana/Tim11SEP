package com.sep.paypal.dto;

import lombok.Data;
import java.util.List;

@Data
public class PaypalOrderRequest {

    private String intent = "CAPTURE";
    private List<PurchaseUnit> purchase_units;
    private ApplicationContext application_context;

    @Data
    public static class PurchaseUnit {
        private Amount amount;
    }

    @Data
    public static class Amount {
        private String currency_code;
        private String value;
    }

    @Data
    public static class ApplicationContext {
        private String return_url;
        private String cancel_url;
    }
}