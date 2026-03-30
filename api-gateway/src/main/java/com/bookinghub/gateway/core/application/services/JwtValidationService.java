package com.bookinghub.gateway.core.application.services;

import com.bookinghub.gateway.core.domain.exceptions.InvalidTokenException;
import com.bookinghub.gateway.core.domain.exceptions.JwtConfigurationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class JwtValidationService {

    @Value("${RSA_PUBLIC_KEY:}")
    private String publicKeyContent;

    @Value("${RSA_PUBLIC_KEY_PATH:}")
    private Resource publicKeyResource;

    public Claims validateTokenAndGetClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getPublicKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid JWT token", e);
        }
    }

    private PublicKey getPublicKey() {
        try {
            String key;
            if (publicKeyResource != null && publicKeyResource.exists()) {
                key = StreamUtils.copyToString(publicKeyResource.getInputStream(), StandardCharsets.UTF_8);
            } else {
                key = publicKeyContent;
            }

            if (key == null || key.isBlank()) {
                throw new JwtConfigurationException("JWT public key content or resource is missing");
            }

            key = key.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                    .replace("-----END RSA PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] encoded = Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (JwtConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtConfigurationException("Could not initialize public key for JWT validation", e);
        }
    }
}
