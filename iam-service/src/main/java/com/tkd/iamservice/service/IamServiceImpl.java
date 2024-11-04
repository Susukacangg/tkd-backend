package com.tkd.iamservice.service;

import com.tkd.iamservice.dto.AuthResponseDto;
import com.tkd.iamservice.entity.IamUserEntity;
import com.tkd.iamservice.entity.UserRole;
import com.tkd.iamservice.repository.UserDao;
import com.tkd.iamservice.utility.IamServiceUtility;
import com.tkd.models.IamUserData;
import com.tkd.models.LoginRequest;
import com.tkd.models.RegistrationRequest;

import com.tkd.models.UserView;
import com.tkd.security.JwtService;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    public AuthResponseDto registerUser(RegistrationRequest regisReq) throws Exception {
        // build new user
        IamUserEntity newUser = IamUserEntity
                .builder()
                .email(regisReq.getEmail())
                .username(regisReq.getUsername())
                .password(passwordEncoder.encode(regisReq.getPassword()))
                .role(UserRole.USER)
                .build();
        AuthResponseDto registerResponse = AuthResponseDto.builder().build();

        IamUserEntity savedUser = userDao.save(newUser); // throws exception if got duplicates
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
                    .maxAge(60 * 60 * 24 * 7)
                    .build();

            registerResponse.setMessage("Successfully registered!");
            registerResponse.setTokenCookie(tokenCookie);
            registerResponse.setRefreshCookie(refreshCookie);
        }

        return registerResponse;
    }

    @Override
    public AuthResponseDto loginUser(LoginRequest loginReq) throws Exception {
        // throws exception if bad credentials
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginReq.getLogin(), loginReq.getPassword()
        ));

        // get username and generate jwt token and refresh token
        Optional<IamUserEntity> retrievedUser = userDao.findByUsernameOrEmail(loginReq.getLogin(), loginReq.getLogin());
        IamUserEntity userDetails = retrievedUser.orElseThrow(() -> new UsernameNotFoundException(loginReq.getLogin() + " not found!"));

        String token = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // set cookies
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
                .maxAge(60 * 60 * 24 * 7)
                .build();

        // set response body and return response
        return AuthResponseDto.builder()
                .message("Successfully logged in!")
                .tokenCookie(tokenCookie)
                .refreshCookie(refreshCookie)
                .build();
    }

    @Override
    public AuthResponseDto logoutUser() {
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

        return AuthResponseDto.builder()
                .message("Successfully logged out!")
                .tokenCookie(tokenCookie)
                .refreshCookie(refreshCookie)
                .build();
    }

    @Override
    public AuthResponseDto refreshToken(Cookie cookie) throws UsernameNotFoundException, IllegalArgumentException, AccountExpiredException {
        // validate the refresh token
        // generate a new jwt token
        // return the jwt token, username, and message
        String currRefreshToken, currUsername;

        // have to catch error because the passed token may be expired
        // and endpoint doesn't need authentication to use
        currRefreshToken = cookie.getValue();
        currUsername = jwtService.extractUsername(currRefreshToken);
        IamUserEntity userDetails = userDao.findByUsername(currUsername)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Refresh token: username %s not found!", currUsername)));

        AuthResponseDto refreshResponse = AuthResponseDto.builder().build();
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

    @Override
    public UserView getUserDetails(String token, boolean includeId) throws UsernameNotFoundException, IllegalArgumentException, AccountExpiredException {
        String username = jwtService.extractUsername(token);

        IamUserEntity userDetails = userDao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Get user details: username %s not found!", username)));

        UserView userView = new UserView();
        userView.setUsername(userDetails.getUsername());
        if (includeId)
            userView.setId(BigDecimal.valueOf(userDetails.getId()));

        return userView;
    }

    @Override
    public IamUserData getIamUserDetails(String loginId) throws UsernameNotFoundException {
        IamUserEntity userDetails = userDao.findByUsernameOrEmail(loginId, loginId)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Get user details: username %s not found!", loginId)));

        IamUserData iamUserData = new IamUserData();
        iamUserData.setUsername(userDetails.getUsername());
        iamUserData.setPassword(userDetails.getPassword());
        iamUserData.setRole(userDetails.getRole().toString());

        return iamUserData;
    }
}
