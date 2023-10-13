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
public class AuthorizeTransaction {

    @NotNull(message = "[" + ErrorCode.AUTH_CODE_BLOCK_DATA_MUST_NOT_BE_NULL + "] request must not be null")
    private AuthCodeBlockData authCodeBlockData;

}
