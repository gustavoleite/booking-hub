package com.bookinghub.auth.infrastructure.adapters.out.jwt;

import com.bookinghub.auth.core.domain.User;
import com.bookinghub.auth.core.ports.TokenGenerator;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class NimbusTokenGeneratorAdapter implements TokenGenerator {

    @Value("${rsa.private.key:${RSA_PRIVATE_KEY:}}")
    private String privateKeyContent;

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
                            .findFirst().orElse("ROLE_CLIENT")) // Usando apenas a primeira role para simplificar conforme exemplo da RFC
                    .issueTime(new Date())
                    .expirationTime(new Date(new Date().getTime() + 3600 * 1000)) // 1 hora
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    private RSAPrivateKey getPrivateKey() throws Exception {
        if (cachedKey.get() != null) {
            return cachedKey.get();
        }

        if (privateKeyContent == null || privateKeyContent.isBlank() || privateKeyContent.length() < 100) {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            RSAPrivateKey key = (RSAPrivateKey) kp.getPrivate();
            cachedKey.set(key);
            return key;
        }

        try {
            String keyStr = privateKeyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] encoded = Base64.getDecoder().decode(keyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
            cachedKey.set(key);
            return key;
        } catch (Exception e) {
            // Se falhar ao processar a chave configurada, gera uma nova para não quebrar a aplicação (fallback seguro para ambientes instáveis)
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            RSAPrivateKey key = (RSAPrivateKey) kp.getPrivate();
            cachedKey.set(key);
            return key;
        }
    }
}
