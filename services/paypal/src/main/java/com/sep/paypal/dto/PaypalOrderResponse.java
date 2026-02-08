package com.sep.paypal.dto;

import lombok.Data;
import java.util.List;

@Data
public class PaypalOrderResponse {

    private String id;
    private String status;
    private List<Link> links;

    @Data
    public static class Link {
        private String href;
        private String rel;
    }
}