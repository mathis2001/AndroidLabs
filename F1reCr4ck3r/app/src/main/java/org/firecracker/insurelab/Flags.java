package org.firecracker.insurelab;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

final class Flags {
    private static final String EMPLOYEE =
        "sqBp7gEYQCUt+68kq812Y1pLcMW959wjBcA85XqDgmpP50nTrjA6drOZNBZDA6oaoJs=";
    private static final String VIP =
        "lRQNwGCWlMt9h36Hlxu1+JH4ahFsLl+gRPUeMj/sjUVE5HBSrpOPaOEYDM4QEGmE";

    static String employee(String campaign) {
        return decode("employee", campaign, EMPLOYEE);
    }

    static String vip(String offer) {
        return decode("vip", offer, VIP);
    }

    private static String decode(String scenario, String token, String encoded) {
        if (token == null || token.isEmpty()) return null;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(
                ("firecracker/" + scenario + "/" + token).getBytes(StandardCharsets.UTF_8));
            byte[] payload = Base64.getDecoder().decode(encoded);
            byte[] nonce = Arrays.copyOfRange(payload, 0, 12);
            byte[] ciphertext = Arrays.copyOfRange(payload, 12, payload.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Arrays.copyOf(digest, 16), "AES"),
                new GCMParameterSpec(128, nonce));
            String value = new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
            return value.startsWith("flag{") && value.endsWith("}") ? value : null;
        } catch (GeneralSecurityException | IllegalArgumentException error) {
            return null;
        }
    }

    private Flags() { }
}
