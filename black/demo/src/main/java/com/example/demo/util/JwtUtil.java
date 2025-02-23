package com.example.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.ArrayList;

@Component
public class JwtUtil {
    // 使用Base64编码的密钥
    private static final String SECRET_KEY = "WVdSdGFXNHhNak0wTlRZM09EbEFiR2x1ZUhrdWJtVjBDZz09";
    private static final long EXPIRATION_TIME = 864_000_000; // 10 days

    // 重载方法：不带权限的 token 生成
    public String generateToken(String username, String role) {
        return generateToken(username, role, new ArrayList<>());
    }

    // 带权限的 token 生成
    public String generateToken(String username, String role, List<String> permissions) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", role);
            claims.put("permissions", permissions);
            return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes())
                .compact();
        } catch (Exception e) {
            throw new RuntimeException("生成token失败: " + e.getMessage());
        }
    }

    public static String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            throw new RuntimeException("提取用户名失败: " + e.getMessage());
        }
    }

    private static Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                .setSigningKey(SECRET_KEY.getBytes())
                .parseClaimsJws(token)
                .getBody();
        } catch (Exception e) {
            throw new RuntimeException("解析token失败: " + e.getMessage());
        }
    }

    private static <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public static Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }
} 