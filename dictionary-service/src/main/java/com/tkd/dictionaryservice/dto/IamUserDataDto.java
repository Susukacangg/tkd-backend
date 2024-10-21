package com.tkd.dictionaryservice.dto;

import lombok.Data;

@Data
public class IamUserDataDto {
    private String username;
    private String password;
    private String role;
}
