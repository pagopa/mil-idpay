package it.pagopa.swclient.mil.idpay.bean;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PublicKey {

    @NotNull
    private KeyType kty;

    @NotNull
    @Size(min = 1, max = 2048)
    private String e;

    @NotNull
    private PublicKeyUse use;

    @NotNull
    @Pattern(regexp = "^[ -~]{1,2048}$")
    private String kid;

    @NotNull
    @Min(value = 0)
    private int exp;

    @NotNull
    @Min(value = 0)
    private int iat;

    @NotNull
    @Size(min = 256, max = 2048)
    private String n;

    private ArrayList<KeyOp> keyOps;
}
