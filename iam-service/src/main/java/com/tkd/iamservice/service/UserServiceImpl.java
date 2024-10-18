package com.tkd.iamservice.service;

import com.tkd.iamservice.entity.UserEntity;
import com.tkd.iamservice.repository.UserDao;
import com.tkd.models.UserAccount;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final JwtService jwtService;
    private final UserDao userDao;

    @Override
    public UserAccount getUserDetails(String token) throws UsernameNotFoundException, ExpiredJwtException {
        String username = jwtService.extractUsername(token);

        UserEntity userDetails = userDao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Get user details: username %s not found!", username)));

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(userDetails.getUsername());

        return userAccount;
    }
}
