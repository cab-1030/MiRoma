package com.miroma.miroma.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utilidad para manejar cookies HTTP-only, Secure y SameSite
 */
@Component
public class CookieUtil {

    @Value("${jwt.expiration:3600000}") // 1 hora por defecto
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7 días por defecto
    private Long refreshTokenExpiration;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${app.cookie.samesite:Lax}")
    private String cookieSameSite;

    /**
     * Crea una cookie HTTP-only, Secure y SameSite para el access token
     */
    public void setAccessTokenCookie(HttpServletResponse response, String token) {
        // Construir el header de cookie
        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append("accessToken=").append(token);
        cookieHeader.append("; Path=/");
        cookieHeader.append("; HttpOnly");
        
        // Solo agregar Secure si está habilitado (en desarrollo puede causar problemas)
        if (cookieSecure) {
            cookieHeader.append("; Secure");
        }
        
        cookieHeader.append("; SameSite=").append(cookieSameSite);
        cookieHeader.append("; Max-Age=").append((int) (accessTokenExpiration / 1000));
        
        response.addHeader("Set-Cookie", cookieHeader.toString());
        
        // Log para debugging (solo en desarrollo)
        System.out.println("Cookie establecida: accessToken (Max-Age: " + (int) (accessTokenExpiration / 1000) + ", SameSite: " + cookieSameSite + ", Secure: " + cookieSecure + ")");
        System.out.println("Cookie header completo: " + cookieHeader.toString());
    }

    /**
     * Crea una cookie HTTP-only, Secure y SameSite para el refresh token
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        // Construir el header de cookie
        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append("refreshToken=").append(token);
        cookieHeader.append("; Path=/");
        cookieHeader.append("; HttpOnly");
        
        // Solo agregar Secure si está habilitado (en desarrollo puede causar problemas)
        if (cookieSecure) {
            cookieHeader.append("; Secure");
        }
        
        cookieHeader.append("; SameSite=").append(cookieSameSite);
        cookieHeader.append("; Max-Age=").append((int) (refreshTokenExpiration / 1000));
        
        response.addHeader("Set-Cookie", cookieHeader.toString());
        
        // Log para debugging (solo en desarrollo)
        System.out.println("Cookie establecida: refreshToken (Max-Age: " + (int) (refreshTokenExpiration / 1000) + ", SameSite: " + cookieSameSite + ", Secure: " + cookieSecure + ")");
        System.out.println("Cookie header completo: " + cookieHeader.toString());
    }

    /**
     * Elimina la cookie del access token
     */
    public void deleteAccessTokenCookie(HttpServletResponse response) {
        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append("accessToken=; Path=/; HttpOnly");
        if (cookieSecure) {
            cookieHeader.append("; Secure");
        }
        cookieHeader.append("; SameSite=").append(cookieSameSite);
        cookieHeader.append("; Max-Age=0");
        response.addHeader("Set-Cookie", cookieHeader.toString());
    }

    /**
     * Elimina la cookie del refresh token
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append("refreshToken=; Path=/; HttpOnly");
        if (cookieSecure) {
            cookieHeader.append("; Secure");
        }
        cookieHeader.append("; SameSite=").append(cookieSameSite);
        cookieHeader.append("; Max-Age=0");
        response.addHeader("Set-Cookie", cookieHeader.toString());
    }

    /**
     * Elimina ambas cookies de tokens
     */
    public void deleteTokenCookies(HttpServletResponse response) {
        deleteAccessTokenCookie(response);
        deleteRefreshTokenCookie(response);
    }
}

