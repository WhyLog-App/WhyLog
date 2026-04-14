package com.whylog.server.global.util.crypto;

import com.whylog.server.domain.git.exception.GitErrorCode;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Converter
@Component
public class AESCryptoConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES";

    @Value("${github.token.encryption.key}")
    private String secretKey;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;

        try {
            validateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encryptedBytes = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new ErrorHandler(GitErrorCode.TOKEN_ENCRYPTION_FAILED);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;

        try {
            validateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decodedBytes = Base64.getDecoder().decode(dbData);
            return new String(cipher.doFinal(decodedBytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ErrorHandler(GitErrorCode.TOKEN_DECRYPTION_FAILED);
        }
    }

    private void validateKey() {
        if (secretKey == null || secretKey.length() != 32) {
            throw new ErrorHandler(GitErrorCode.TOKEN_ENCRYPTION_FAILED); // 키 설정 오류도 암호화 에러로 처리
        }
    }
}