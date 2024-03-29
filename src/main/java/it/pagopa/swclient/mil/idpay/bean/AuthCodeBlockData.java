package it.pagopa.swclient.mil.idpay.bean;

import it.pagopa.swclient.mil.idpay.ErrorCode;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class AuthCodeBlockData {

    @NotNull(message = "[" + ErrorCode.ERROR_KID_MUST_NOT_BE_NULL + "] kid must not be null")
    @Pattern(regexp = "^[ -~]{1,2048}$",
            message = "[" + ErrorCode.ERROR_KID_MUST_MATCH_REGEXP + "] kid must match \"{regexp}\"")
    private String kid;

    @NotNull(message = "[" + ErrorCode.ERROR_ENCSESSIONKEY_MUST_NOT_BE_NULL + "] encSessionKey must not be null")
    @Size(min = 256, max = 2048, message = "[" + ErrorCode.ERROR_ENCSESSIONKEY_MUST_BE_BETWEEN + "] encSessionKey must be between than {min} and {max}")
    private byte[] encSessionKey;

    @NotNull(message = "[" + ErrorCode.ERROR_AUTHCODEBLOCK_MUST_NOT_BE_NULL + "] authCodeBlock must not be null")
    @Size(min = 16, max = 16, message = "[" + ErrorCode.ERROR_AUTHCODEBLOCK_MUST_BE_BETWEEN + "] authCodeBlock must be between than {min} and {max}")
    private byte[] authCodeBlock;

}
