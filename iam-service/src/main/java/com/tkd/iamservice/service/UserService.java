package com.tkd.iamservice.service;

import com.tkd.models.UserAccount;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService {
    UserAccount  getUserDetails(String token) throws UsernameNotFoundException, IllegalArgumentException, AccountExpiredException;
}
