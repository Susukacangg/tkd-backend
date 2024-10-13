package com.tkd.dictionaryservice.service;

import com.tkd.dictionaryservice.dto.LogoutResponse;
import com.tkd.dictionaryservice.dto.UserSession;
import com.tkd.dictionaryservice.entity.UserEntity;
import com.tkd.dictionaryservice.entity.UserRole;
import com.tkd.dictionaryservice.repository.IamDao;
import com.tkd.models.LoginRequest;
import com.tkd.models.LoginResponse;
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

    private final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @Override
    public String registerUser(RegistrationRequest regisReq) throws Exception {
        // build new user
        UserEntity newUser = UserEntity
                .builder()
                .email(regisReq.getEmail())
                .username(regisReq.getUsername())
                .password(passwordEncoder.encode(regisReq.getPassword()))
                .role(UserRole.USER)
                .build();

        UserEntity savedUser = iamDao.save(newUser); // throws exception if got duplicates
        if(savedUser.getId() > 0)
            return "Successfully registered!"; // set message

        return null;
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
        ResponseCookie responseCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                .build();

        // set response body and return response
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUsername(retrievedUser.get().getUsername());
        loginResponse.setToken(jwtToken);
        loginResponse.setMessage("Successfully logged in!");

        return UserSession.builder()
                .loginResponse(loginResponse)
                .responseCookie(responseCookie)
                .build();
    }

    @Override
    public LogoutResponse logoutUser() {
        ResponseCookie responseCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        return LogoutResponse.builder()
                .message("Successfully logged out!")
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
