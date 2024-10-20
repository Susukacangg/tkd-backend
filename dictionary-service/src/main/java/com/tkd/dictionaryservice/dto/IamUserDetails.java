package com.tkd.dictionaryservice.dto;

import lombok.Data;

@Data
public class IamUserDetails {
    private String username;

    private String password;

    private String role;
}
