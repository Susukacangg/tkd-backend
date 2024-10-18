package com.tkd.iamservice.service;

import com.tkd.models.UserAccount;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService {
    UserAccount  getUserDetails(String token) throws ExpiredJwtException, UsernameNotFoundException;
}
