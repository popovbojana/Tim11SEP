package com.sep.banksimulator.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckBalanceResponse {
    private boolean sufficient;
    private String reason;  
}