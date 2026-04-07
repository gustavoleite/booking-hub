package com.bookinghub.auth.infrastructure.adapters.out.jwt;

import com.bookinghub.auth.core.domain.User;
import com.bookinghub.auth.core.ports.TokenGenerator;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class NimbusTokenGeneratorAdapter implements TokenGenerator {

    @Value("${rsa.private.key:${RSA_PRIVATE_KEY:}}")
    private String privateKeyContent;

    @Value("${rsa.private.key-path:${RSA_PRIVATE_KEY_PATH:}}")
    private Resource privateKeyResource;

    private final AtomicReference<RSAPrivateKey> cachedKey = new AtomicReference<>();

    @Override
    public String generateToken(User user) {
        try {
            RSAPrivateKey privateKey = getPrivateKey();
            JWSSigner signer = new RSASSASigner(privateKey);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getId().toString())
                    .claim("email", user.getEmail())
                    .claim("role", user.getRoles().stream()
                            .map(Enum::name)
                            .findFirst().orElse("ROLE_CLIENT")) // primeira role (RFC)
                    .issueTime(new Date())
                    .expirationTime(new Date(new Date().getTime() + 3600 * 1000)) // 1 hora
                    .build();

            SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);
            signedJwt.sign(signer);

            return signedJwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    private RSAPrivateKey getPrivateKey() throws Exception {
        if (cachedKey.get() != null) {
            return cachedKey.get();
        }

        String content = null;
        if (privateKeyResource != null && privateKeyResource.exists()) {
            content = StreamUtils.copyToString(
                    privateKeyResource.getInputStream(), StandardCharsets.UTF_8);
        } else if (privateKeyContent != null && !privateKeyContent.isBlank()) {
            content = privateKeyContent;
        }

        if (content == null || content.isBlank() || content.length() < 100) {
            return generateAndCacheKey();
        }

        try {
            String keyStr = content
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] encoded = Base64.getDecoder().decode(keyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
            cachedKey.set(key);
            return key;
        } catch (Exception e) {
            System.err.println(
                    "Failed to load private key, generating fallback: " + e.getMessage());
            return generateAndCacheKey();
        }
    }

    private RSAPrivateKey generateAndCacheKey() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        RSAPrivateKey key = (RSAPrivateKey) kp.getPrivate();
        cachedKey.set(key);
        return key;
    }
}
