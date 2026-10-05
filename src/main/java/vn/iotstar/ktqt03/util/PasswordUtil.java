package vn.iotstar.ktqt03.util;
import java.security.*;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONS = 600_000;
    private PasswordUtil() {}
    private static byte[] derive(String raw, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(raw.toCharArray(), salt, ITERATIONS, 192);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        } finally { spec.clearPassword(); }
    }
    public static String hash(String raw) {
        byte[] salt = new byte[12]; RANDOM.nextBytes(salt);
        Base64.Encoder b = Base64.getEncoder();
        return b.encodeToString(salt) + ":" + b.encodeToString(derive(raw, salt));
    }
    public static boolean matches(String raw, String stored) {
        if (raw == null || stored == null) return false;
        try {
            String[] parts = stored.split(":", -1);
            if (parts.length != 2) return false;
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);
            return salt.length == 12 && expected.length == 24
                && MessageDigest.isEqual(expected, derive(raw, salt));
        } catch (IllegalArgumentException ex) { return false; }
    }
}
