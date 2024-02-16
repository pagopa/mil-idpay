package it.pagopa.swclient.mil.idpay.azurekeyvault.util;

import it.pagopa.swclient.mil.idpay.bean.PublicKeyIDPay;
import jakarta.enterprise.context.ApplicationScoped;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@ApplicationScoped
public class EncryptUtil {

    public String encryptSessionKeyForIdpay(PublicKeyIDPay publicKeyIDPay, String sessionKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String modulusBase64 = publicKeyIDPay.getN();
        String exponentBase64 = publicKeyIDPay.getE();

        // Decode Base64 values in byte
        byte[] modulusBytes = decodeBase64UrlOrBase64(modulusBase64);
        byte[] exponentBytes = decodeBase64UrlOrBase64(exponentBase64);

        // Create specific RSA public key
        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(
                new BigInteger(1, modulusBytes),
                new BigInteger(1, exponentBytes)
        );

        // Get RSA public key
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey rsaPublicKey = keyFactory.generatePublic(rsaPublicKeySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);

        byte[] sessionKeyBytes = decodeBase64UrlOrBase64(sessionKey);
        byte[] encryptedSessionKeyBytes = cipher.doFinal(sessionKeyBytes);

        // encryptedSessionKeyBytes contains encrypted session key
        return Base64.getEncoder().encodeToString(encryptedSessionKeyBytes);
    }

    private byte[] decodeBase64UrlOrBase64(String base64) {
        return (base64.contains("-") || base64.contains("_") ? Base64.getUrlDecoder() : Base64.getDecoder()).decode(base64);
    }
}
