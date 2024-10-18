package com.tkd.iamservice.service;

import com.tkd.iamservice.dto.AuthResponse;
import com.tkd.iamservice.entity.UserEntity;
import com.tkd.iamservice.entity.UserRole;
import com.tkd.iamservice.repository.UserDao;
import com.tkd.iamservice.utility.IamServiceUtility;
import com.tkd.models.LoginRequest;
import com.tkd.models.RegistrationRequest;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
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
    private final UserDao userDao;
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
        AuthResponse registerResponse = AuthResponse.builder().build();

        UserEntity savedUser = userDao.save(newUser); // throws exception if got duplicates
        if(savedUser.getId() > 0) {
            String token = jwtService.generateToken(savedUser);
            String refreshToken = jwtService.generateRefreshToken(savedUser);

            ResponseCookie tokenCookie = ResponseCookie.from(IamServiceUtility.TOKEN_COOKIE_KEY, token)
                    .httpOnly(true)
                    .sameSite("None")
                    .secure(true)
                    .path("/")
                    .maxAge(60 * 15)
                    .build();

            ResponseCookie refreshCookie = ResponseCookie.from(IamServiceUtility.REFRESH_TOKEN_COOKIE_KEY, refreshToken)
                    .httpOnly(true)
                    .sameSite("None")
                    .secure(true)
                    .path("/")
                    .maxAge(60 * 15)
                    .build();

            registerResponse.setMessage("Successfully registered!");
            registerResponse.setTokenCookie(tokenCookie);
            registerResponse.setRefreshCookie(refreshCookie);
        }

        return registerResponse;
    }

    @Override
    public AuthResponse loginUser(LoginRequest loginReq) throws Exception {
        // throws exception if bad credentials
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginReq.getLogin(), loginReq.getPassword()
        ));

        // get username and generate jwt token and refresh token
        Optional<UserEntity> retrievedUser = userDao.findByUsernameOrEmail(loginReq.getLogin(), loginReq.getLogin());
        UserEntity userDetails = retrievedUser.orElseThrow(() -> new UsernameNotFoundException(loginReq.getLogin() + " not found!"));

        String token = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // set cookies
        ResponseCookie tokenCookie = ResponseCookie.from(IamServiceUtility.TOKEN_COOKIE_KEY, token)
                .httpOnly(true)
                .sameSite("None")
                .secure(true)
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(IamServiceUtility.REFRESH_TOKEN_COOKIE_KEY, refreshToken)
                .httpOnly(true)
                .sameSite("None")
                .secure(true)
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                .build();

        // set response body and return response
        return AuthResponse.builder()
                .message("Successfully logged in!")
                .tokenCookie(tokenCookie)
                .refreshCookie(refreshCookie)
                .build();
    }

    @Override
    public AuthResponse logoutUser() {
        ResponseCookie tokenCookie = ResponseCookie.from(IamServiceUtility.TOKEN_COOKIE_KEY, "")
                .httpOnly(true)
                .sameSite("None")
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(IamServiceUtility.REFRESH_TOKEN_COOKIE_KEY, "")
                .httpOnly(true)
                .sameSite("None")
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return AuthResponse.builder()
                .message("Successfully logged out!")
                .tokenCookie(tokenCookie)
                .refreshCookie(refreshCookie)
                .build();
    }

    @Override
    public AuthResponse refreshToken(Cookie cookie) throws ExpiredJwtException, UsernameNotFoundException {
        // validate the refresh token
        // generate a new jwt token
        // return the jwt token, username, and message
        String currRefreshToken, currUsername;

        // have to catch error because the passed token may be expired
        // and endpoint doesn't need authentication to use
        currRefreshToken = cookie.getValue();
        currUsername = jwtService.extractUsername(currRefreshToken);
        UserEntity userDetails = userDao.findByUsername(currUsername)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Refresh token: username %s not found!", currUsername)));

        AuthResponse refreshResponse = AuthResponse.builder().build();
        String token;
        if (jwtService.isTokenValid(currRefreshToken, userDetails)) {
            token = jwtService.generateToken(userDetails);

            ResponseCookie tokenCookie = ResponseCookie.from(IamServiceUtility.TOKEN_COOKIE_KEY, token)
                    .httpOnly(true)
                    .sameSite("None")
                    .secure(true)
                    .path("/")
                    .maxAge(60 * 15)
                    .build();

            refreshResponse.setTokenCookie(tokenCookie);
            refreshResponse.setMessage("Refreshed token");
        }

        return refreshResponse;
    }

    @Override
    public Boolean checkUsernameAvailable(String username) {
        return userDao.findByUsername(username).isPresent();
    }

    @Override
    public Boolean checkEmailAvailable(String email) {
        return userDao.findByEmail(email).isPresent();
    }
}
