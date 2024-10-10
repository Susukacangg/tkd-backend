package com.tkd.dictionaryservice.service;

import com.tkd.dictionaryservice.dto.UserSession;
import com.tkd.dictionaryservice.entity.UserEntity;
import com.tkd.dictionaryservice.entity.UserRole;
import com.tkd.dictionaryservice.repository.IamDao;
import com.tkd.models.AuthResponse;
import com.tkd.models.LoginRequest;
import com.tkd.models.RegistrationRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IamServiceImpl implements IamService {
    private final PasswordEncoder passwordEncoder;
    private final IamDao iamDao;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse registerUser(RegistrationRequest regisReq) throws Exception {
        // build new user
        UserEntity newUser = UserEntity
                .builder()
                .email(regisReq.getEmail())
                .username(regisReq.getUsername())
                .password(passwordEncoder.encode(regisReq.getPassword()))
                .role(UserRole.USER)
                .build();

        UserEntity savedUser = iamDao.save(newUser); // throws exception if got duplicates
        AuthResponse response = new AuthResponse();
        if(savedUser.getId() > 0)
            response.setMessage("Successfully registered!"); // set response info message

        return response;
    }

    @Override
    public UserSession loginUser(LoginRequest loginReq) throws Exception {
        // throws exception if bad credentials
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginReq.getLogin(), loginReq.getPassword()
        ));

        // get username and generate jwt token and refresh token
        Optional<UserEntity> retrievedUser = iamDao.findByUsernameOrEmail(loginReq.getLogin(), loginReq.getLogin());
        retrievedUser.orElseThrow(() -> new UsernameNotFoundException(loginReq.getLogin() + " not found!"));

        String jwtToken = jwtService.generateToken(retrievedUser.get());
        String refreshToken = jwtService.generateRefreshToken(retrievedUser.get());

        // set cookies
        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                .build();

        // set response body and return response
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(jwtToken);
        authResponse.setMessage("Successfully logged in!");

        return UserSession.builder()
                .authResponse(authResponse)
                .responseCookie(responseCookie)
                .build();
    }

    @Override
    public Boolean checkUsernameAvailable(String username) {
        return iamDao.findByUsername(username).isPresent();
    }

    @Override
    public Boolean checkEmailAvailable(String email) {
        return iamDao.findByEmail(email).isPresent();
    }
}
