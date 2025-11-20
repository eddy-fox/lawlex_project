package com.soldesk.team_project.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;

@Component
public class JwtProvider {
    
    @Value("${jwt.secret}")
    private String secretKey;

    private final long EXPIRATION_MS = 1000 * 60 * 60; // 1시간

    public String createToken(UserBase user) {

        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("idx", user.getIdx())
                .claim("name", user.getName())
                .claim("userType", user.getUserType())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key)
                .compact();
    }

    /**
     * JWT 토큰 검증 및 Claims 추출
     * @param token JWT 토큰
     * @return Claims 객체 (검증 실패 시 null)
     */
    public Claims validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SecurityException | MalformedJwtException e) {
            // 잘못된 JWT 서명
            return null;
        } catch (ExpiredJwtException e) {
            // 만료된 JWT 토큰
            return null;
        } catch (UnsupportedJwtException e) {
            // 지원되지 않는 JWT 토큰
            return null;
        } catch (IllegalArgumentException e) {
            // JWT 토큰이 잘못됨
            return null;
        }
    }

    /**
     * JWT에서 idx 추출
     */
    public Integer getIdxFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) return null;
        return claims.get("idx", Integer.class);
    }

    /**
     * JWT에서 userType 추출
     */
    public String getUserTypeFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) return null;
        return claims.get("userType", String.class);
    }

    /**
     * JWT에서 email 추출
     */
    public String getEmailFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) return null;
        return claims.getSubject();
    }
}
