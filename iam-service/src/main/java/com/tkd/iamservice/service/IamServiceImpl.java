package com.tkd.iamservice.service;

import com.tkd.iamservice.dto.AuthResponseDto;
import com.tkd.iamservice.entity.IamUserEntity;
import com.tkd.iamservice.entity.UserRole;
import com.tkd.iamservice.repository.UserDao;
import com.tkd.models.IamUserData;
import com.tkd.models.LoginRequest;
import com.tkd.models.RegistrationRequest;

import com.tkd.models.UserView;
import com.tkd.security.JwtService;
import com.tkd.security.SecurityUtility;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IamServiceImpl implements IamService {
    private final PasswordEncoder passwordEncoder;
    private final UserDao userDao;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final String COOKIE_DOMAIN = "thekadazandusundictionary.com";

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
            HashMap<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("role", savedUser.getRole());
            String token = jwtService.generateToken(extraClaims, savedUser);
            String refreshToken = jwtService.generateRefreshToken(savedUser);

            ResponseCookie tokenCookie = ResponseCookie.from(SecurityUtility.TOKEN_COOKIE_KEY, token)
                    .httpOnly(true)
                    .sameSite("Lax")
                    .secure(true)
                    .path("/")
                    .domain(COOKIE_DOMAIN)
                    .maxAge(60 * 15)
                    .build();

            ResponseCookie refreshCookie = ResponseCookie.from(SecurityUtility.REFRESH_TOKEN_COOKIE_KEY, refreshToken)
                    .httpOnly(true)
                    .sameSite("Lax")
                    .secure(true)
                    .path("/")
                    .domain(COOKIE_DOMAIN)
                    .maxAge(60 * 60 * 24 * 7)
                    .build();

            registerResponse.setMessage("Successfully registered!");
            registerResponse.setTokenCookie(tokenCookie);
            registerResponse.setRefreshCookie(refreshCookie);
        }

        return registerResponse;
    }

    @Override
    public AuthResponseDto loginUser(LoginRequest loginReq) throws UsernameNotFoundException, AuthenticationException {
        // get username and generate jwt token and refresh token
        Optional<IamUserEntity> retrievedUser = userDao.findByUsernameOrEmail(loginReq.getLogin(), loginReq.getLogin());
        IamUserEntity userDetails = retrievedUser.orElseThrow(() -> new UsernameNotFoundException(loginReq.getLogin() + " not found!"));

        // throws exception if bad credentials
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginReq.getLogin(), loginReq.getPassword()
        ));

        HashMap<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", userDetails.getRole());
        String token = jwtService.generateToken(extraClaims, userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // set cookies
        ResponseCookie tokenCookie = ResponseCookie.from(SecurityUtility.TOKEN_COOKIE_KEY, token)
                .httpOnly(true)
                .sameSite("Lax")
                .secure(true)
                .path("/")
                .domain(COOKIE_DOMAIN)
                .maxAge(60 * 15)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(SecurityUtility.REFRESH_TOKEN_COOKIE_KEY, refreshToken)
                .httpOnly(true)
                .sameSite("Lax")
                .secure(true)
                .path("/")
                .domain(COOKIE_DOMAIN)
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
        ResponseCookie tokenCookie = ResponseCookie.from(SecurityUtility.TOKEN_COOKIE_KEY, "")
                .httpOnly(true)
                .sameSite("Lax")
                .secure(true)
                .path("/")
                .domain(COOKIE_DOMAIN)
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(SecurityUtility.REFRESH_TOKEN_COOKIE_KEY, "")
                .httpOnly(true)
                .sameSite("Lax")
                .secure(true)
                .path("/")
                .domain(COOKIE_DOMAIN)
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
            HashMap<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("role", userDetails.getRole());
            token = jwtService.generateToken(extraClaims, userDetails);

            ResponseCookie tokenCookie = ResponseCookie.from(SecurityUtility.TOKEN_COOKIE_KEY, token)
                    .httpOnly(true)
                    .sameSite("Lax")
                    .secure(true)
                    .path("/")
                    .domain(COOKIE_DOMAIN)
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
    public UserView getUserDetails(String token, boolean includeId) {
        String username = jwtService.extractUsername(token);

        IamUserEntity userDetails = userDao.findByUsername(username)
                .orElse(null);

        if(userDetails == null)
            return null;

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

    @Override
    public Boolean adminCheck(String token) {
        if(token.isEmpty())
            return false;

        return jwtService.extractRole(token).equals("ADMIN");
    }

    @Override
    public ResponseCookie generateCsrfCookie() {
        String csrfToken = UUID.randomUUID().toString();

        return ResponseCookie.from(SecurityUtility.CSRF_TOKEN_COOKIE_KEY, csrfToken)
                .httpOnly(false)
                .sameSite("Lax")
                .secure(true)
                .path("/")
                .domain(COOKIE_DOMAIN)
                .maxAge(60 * 15)
                .build();
    }

    @Override
    public UserView getUser(Long userId) {
        UserView userView = null;

        IamUserEntity userEntity = userDao.findByIdEquals(userId).orElse(null);

        if (userEntity != null) {
            userView = new UserView();
            userView.setUsername(userEntity.getUsername());
        }

        return userView;
    }
}
