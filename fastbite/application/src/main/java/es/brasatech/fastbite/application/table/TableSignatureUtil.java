package es.brasatech.fastbite.application.table;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Signs the links printed as QR codes on tables, so a guest can only order to a table by being at it.
 * A signature names both the restaurant and the table: one restaurant's code never opens another's table.
 */
@Component
public class TableSignatureUtil {

    /** Used when TABLE_QR_SECRET is not set; fine on a laptop, never for printed codes. */
    public static final String DEVELOPMENT_SECRET = "local-development-only-table-secret";

    private static final Logger LOGGER = Logger.getLogger(TableSignatureUtil.class.getName());

    private final SecretKeySpec key;

    public TableSignatureUtil(@Value("${fastbite.table.secret:" + DEVELOPMENT_SECRET + "}") String secret) {
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        if (DEVELOPMENT_SECRET.equals(secret)) {
            LOGGER.warning("Table QR codes are signed with the development secret. Set TABLE_QR_SECRET before printing any.");
        }
    }

    public String generateSignature(String tenantId, String tableId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] signature = mac.doFinal(signed(tenantId, tableId).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(signature);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Error computing signature", e);
        }
    }

    public boolean isValid(String tenantId, String tableId, String token) {
        if (tenantId == null || tableId == null || token == null) {
            return false;
        }
        byte[] expected = generateSignature(tenantId, tableId).getBytes(StandardCharsets.UTF_8);
        byte[] given = token.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, given);
    }

    private static String signed(String tenantId, String tableId) {
        return (tenantId == null ? "" : tenantId.toLowerCase(Locale.ROOT)) + ":" + tableId;
    }
}
