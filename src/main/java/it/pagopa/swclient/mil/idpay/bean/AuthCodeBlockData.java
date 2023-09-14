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
            message = "[" + ErrorCode.ERROR_TIMESTAMP_MUST_MATCH_REGEXP + "] timestamp must match \"{regexp}\"")
    private String kid;

    @NotNull(message = "[" + ErrorCode.ERROR_ENCSESSIONKEY_MUST_NOT_BE_NULL + "] encSessionKey must not be null")
    @Size(min = 256, max = 2048)
    private String encSessionKey;

    @NotNull(message = "[" + ErrorCode.ERROR_AUTHCODEBLOCK_MUST_NOT_BE_NULL + "] authCodeBlock must not be null")
    @Size(min = 16, max = 16)
    private String authCodeBlock;

}
