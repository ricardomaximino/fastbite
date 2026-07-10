package es.brasatech.fastbite.application.table;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public class TableSignatureUtil {

    private final String secret;

    public TableSignatureUtil(@Value("${fastbite.table.secret:SuperSecretKeyForTableOrderingSignatures!!!}") String secret) {
        this.secret = secret;
    }

    /**
     * Generates a signature for a given table identifier.
     */
    public String generateSignature(String tableId) {
        if (tableId == null) {
            return "";
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(tableId.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error computing signature", e);
        }
    }

    /**
     * Validates a signature against a table identifier.
     */
    public boolean isValid(String tableId, String token) {
        if (tableId == null || token == null) {
            return false;
        }
        String computed = generateSignature(tableId);
        return computed.equalsIgnoreCase(token);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
