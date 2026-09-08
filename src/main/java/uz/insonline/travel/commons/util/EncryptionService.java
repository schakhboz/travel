package uz.insonline.travel.commons.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

public class EncryptionService {
    private final SecretKey secretKey;
    private final Cipher cipher;

    public EncryptionService(String key) throws GeneralSecurityException {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        DESKeySpec desKeySpec = new DESKeySpec(keyBytes);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        secretKey = keyFactory.generateSecret(desKeySpec);
        cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
    }

    public String encrypt(String original) throws GeneralSecurityException {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(original.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public String decrypt(String cypher) throws GeneralSecurityException {
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] encryptedData = Base64.getDecoder().decode(cypher);
        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }
}
