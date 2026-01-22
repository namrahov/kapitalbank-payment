package com.kapitalbank.payment.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Component
public class TokenUtil {

    private static final String ALGORITHM = "AES";
    private final SecretKey secretKey;

    public TokenUtil(@Value("${token.secret-key}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(), ALGORITHM);
    }

    public String generateSHA(String text) {
        StringBuilder stringBuffer = new StringBuilder();

        try {
            MessageDigest messageDigestdigest = MessageDigest.getInstance("SHA-256");

            byte[] hash = messageDigestdigest.digest(text.getBytes(StandardCharsets.UTF_8));
            for (byte b : hash) {
                String hex = Integer.toHexString((b & 0xff));
                if (hex.length() == 1) stringBuffer.append('0');
                stringBuffer.append(hex);

            }
        } catch (Exception e) {
        }

        return stringBuffer.toString();
    }

    public String generateToken() {
        UUID randomUUID = UUID.randomUUID();
        return generateSHA(randomUUID.toString());
    }

    public String generateToken(String email) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(email.getBytes());

            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public String parseToken(String token) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decodedBytes = Base64.getDecoder().decode(token);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            return new String(decryptedBytes);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

}
