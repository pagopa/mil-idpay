package it.pagopa.swclient.mil.idpay.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

import java.util.ArrayList;

@RegisterForReflection
@Data
@AllArgsConstructor
public class PublicKeyIDPay {
    /*
     * Public exponent
     */
    private String e;

    /*
     * Public key use
     */
    private PublicKeyUse use;

    /*
     * Key ID
     */
    private String kid;

    /*
     * Modulus
     */
    private String n;

    /*
     * Key type
     */
    private KeyType kty;

    /*
     * Expiration time
     */
    private long exp;

    /*
     * Issued at
     */
    private long iat;

    private ArrayList<KeyOp> keyOps;
}
