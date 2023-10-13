package it.pagopa.swclient.mil.idpay.bean;

import it.pagopa.swclient.mil.idpay.ErrorCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"nis"})
public class VerifyCie {

    @Pattern(regexp = "^[0-9]{12}$")
    @NotNull(message = "[" + ErrorCode.VERIFY_CIE_MUST_NOT_BE_EMPTY + "] request must not be empty")
    private String nis;

    @NotNull(message = "[" + ErrorCode.CIE_PUBLIC_KEY_MUST_NOT_BE_NULL + "] request must not be null")
    private byte[] ciePublicKey;

    @NotNull(message = "[" + ErrorCode.SIGNATURE_MUST_NOT_BE_NULL + "] request must not be null")
    private byte[] signature;

    @NotNull(message = "[" + ErrorCode.SOD_MUST_NOT_BE_NULL + "] request must not be null")
    private byte[] sod;
}
