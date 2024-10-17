package com.tkd.dictionaryservice.service;

import com.tkd.models.UserAccount;

public interface UserService {
    UserAccount  getUserDetails(String token);
}
