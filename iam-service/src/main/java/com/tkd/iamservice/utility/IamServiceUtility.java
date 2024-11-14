package com.tkd.iamservice.utility;


import com.tkd.security.SecurityUtility;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class IamServiceUtility {

    public static Cookie getAccessTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Cookie accessTokenCookie = null;
        if (cookies != null)
            for (Cookie cookie : cookies)
                if (cookie.getName().equals(SecurityUtility.TOKEN_COOKIE_KEY))
                    accessTokenCookie = cookie;

        return accessTokenCookie;
    }

    public static Cookie getRefreshTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Cookie accessTokenCookie = null;
        if (cookies != null)
            for (Cookie cookie : cookies)
                if (cookie.getName().equals(SecurityUtility.REFRESH_TOKEN_COOKIE_KEY))
                    accessTokenCookie = cookie;

        return accessTokenCookie;
    }
}
