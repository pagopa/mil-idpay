package it.pagopa.swclient.mil.idpay.bean;

import it.pagopa.swclient.mil.idpay.ErrorCode;
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

    @Pattern(regexp = "^[0-9]{12}$",
            message = "[" + ErrorCode.ERROR_NIS_MUST_MATCH_REGEXP + "] nis must match \"{regexp}\"")
    @NotNull(message = "[" + ErrorCode.VERIFY_CIE_MUST_NOT_BE_EMPTY + "] request must not be empty")
    private String nis;

    @NotNull(message = "[" + ErrorCode.CIE_PUBLIC_KEY_MUST_NOT_BE_NULL + "] request must not be null")
    @Size(min = 1, max = 32767, message = "[" + ErrorCode.ERROR_CIE_PUBLIC_KEY_MUST_BE_BETWEEN + "] ciePublicKey must be between than {min} and {max}")
    private byte[] ciePublicKey;

    @NotNull(message = "[" + ErrorCode.SIGNATURE_MUST_NOT_BE_NULL + "] request must not be null")
    @Size(min = 1, max = 32767, message = "[" + ErrorCode.ERROR_SIGNATURE_MUST_BE_BETWEEN + "] signature must be between than {min} and {max}")
    private byte[] signature;

    @NotNull(message = "[" + ErrorCode.SOD_MUST_NOT_BE_NULL + "] request must not be null")
    @Size(min = 1, max = 32767, message = "[" + ErrorCode.ERROR_SOD_MUST_BE_BETWEEN + "] sod must be between than {min} and {max}")
    private byte[] sod;
}
