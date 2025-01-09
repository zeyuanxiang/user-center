package com.xzy.usercenter.http;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CookieUtil {

    private static final String COOKIE_NAME = "USER_LOGIN_STATE";
    private static final String COOKIE_DOMAIN = "api.com";

    /**
     * 写cookie
     * @param response 使用响应对象将cookie写到浏览器上
     * @param token 就是sessionID,也就是cookie的值，这个值是唯一的就行，使用UUID也可以
     */
    public static void writeCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setDomain(COOKIE_DOMAIN);
        cookie.setPath("/");
        //设置生效时间 0无效 -1永久有效，时间是秒 生存时间为一天
        cookie.setMaxAge(60 * 60 * 24);
        //设置安全机制
        cookie.setHttpOnly(true);
        log.info("写cookie name: {}, value: {}", cookie.getName(), cookie.getValue());
        response.addCookie(cookie);
    }

    /**
     * 读取cookie
     * @param request
     * @return
     */
    public static String readUserLoginCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.info("读取cookie name: {}, value: {}", cookie.getName(), cookie.getValue());
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static void delCookie(HttpServletResponse response, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    cookie.setDomain(COOKIE_DOMAIN);

                    response.addCookie(cookie);
                    return;
                }
            }
        }
    }
}
