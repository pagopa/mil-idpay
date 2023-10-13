package it.pagopa.swclient.mil.idpay.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

import java.util.List;

@RegisterForReflection
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicKeyIDPay {
    /*
     * Public exponent
     */
    private byte[] e;

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
    private byte[] n;

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

    private List<KeyOp> keyOps;
}
