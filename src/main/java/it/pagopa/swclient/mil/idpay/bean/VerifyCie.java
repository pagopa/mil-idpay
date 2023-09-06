package it.pagopa.swclient.mil.idpay.bean;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"nis"})
public class VerifyCie {

    @NotNull
    @Pattern(regexp = "^[0-9]{12}$")
    private String nis;

    @NotNull
    @Size(min = 1, max = 32767)
    private String ciePublicKey;

    @NotNull
    @Size(min = 1, max = 32767)
    private String signature;

    @NotNull
    @Size(min = 1, max = 32767)
    private String sod;
}
