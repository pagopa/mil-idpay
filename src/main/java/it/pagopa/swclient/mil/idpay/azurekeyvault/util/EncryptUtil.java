package it.pagopa.swclient.mil.idpay.azurekeyvault.util;

import it.pagopa.swclient.mil.idpay.bean.PublicKeyIDPay;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

@ApplicationScoped
public class EncryptUtil {

    public String encryptSessionKeyForIdpay(PublicKeyIDPay publicKeyIDPay, String sessionKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {// Chiave pubblica RSA codificata in base64 (sostituisci con la tua chiave pubblica)
        String modulusBase64 = publicKeyIDPay.getN();
        String exponentBase64 = publicKeyIDPay.getE();

        // Decode Base64 values in byte
        byte[] modulusBytes = Base64.getDecoder().decode(modulusBase64);
        byte[] exponentBytes = Base64.getDecoder().decode(exponentBase64);

        // Create specific RSA public key
        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(
                new BigInteger(1, modulusBytes),
                new BigInteger(1, exponentBytes)
        );

        // Get RSA public key
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey rsaPublicKey = keyFactory.generatePublic(rsaPublicKeySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        byte[] sessionKeyBytes = sessionKey.getBytes(StandardCharsets.UTF_8);
        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
        byte[] encryptedSessionKeyBytes = cipher.doFinal(sessionKeyBytes);

        // encryptedSessionKeyBytes contains encrypted session key
        return Base64.getEncoder().encodeToString(encryptedSessionKeyBytes);
    }
}
