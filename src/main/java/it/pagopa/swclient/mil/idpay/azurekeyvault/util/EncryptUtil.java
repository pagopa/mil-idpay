package it.pagopa.swclient.mil.idpay.azurekeyvault.util;

import it.pagopa.swclient.mil.idpay.bean.PublicKeyIDPay;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

@ApplicationScoped
public class EncryptUtil {

    public String encryptSessionKeyForIdpay(PublicKeyIDPay publicKeyIDPay, String sessionKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        String modulusBase64 = publicKeyIDPay.getN();
        String exponentBase64 = publicKeyIDPay.getE();

        // Decode Base64 values in byte
        byte[] modulusBytes = Base64.getUrlDecoder().decode(modulusBase64);
        byte[] exponentBytes = Base64.getUrlDecoder().decode(exponentBase64);

        // Create specific RSA public key
        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(
                new BigInteger(1, modulusBytes),
                new BigInteger(1, exponentBytes)
        );

        // Get RSA public key
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey rsaPublicKey = keyFactory.generatePublic(rsaPublicKeySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1",
                new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey, oaepParams);

        byte[] sessionKeyBytes = sessionKey.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedSessionKeyBytes = cipher.doFinal(sessionKeyBytes);

        // encryptedSessionKeyBytes contains encrypted session key
        return Base64.getEncoder().encodeToString(encryptedSessionKeyBytes);
    }
}
