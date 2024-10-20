package com.tkd.dictionaryservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserAccount {
    private BigDecimal id;
    private String username;
}
