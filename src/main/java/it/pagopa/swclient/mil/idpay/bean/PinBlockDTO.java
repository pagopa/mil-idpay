package it.pagopa.swclient.mil.idpay.bean;

import it.pagopa.swclient.mil.idpay.ErrorCode;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class PinBlockDTO {

    @NotNull(message = "[" + ErrorCode.ERROR_ENCRYPTEDPINBLOCK_MUST_NOT_BE_NULL + "] pinBlock must not be null")
    private String pinBlock;

    @NotNull(message = "[" + ErrorCode.ERROR_ENCRYPTEDKEY_MUST_NOT_BE_NULL + "] encryptedKey must not be null")
    private String encryptedKey;

}
