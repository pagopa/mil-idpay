package it.pagopa.swclient.mil.idpay.bean;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class AuthorizeTransaction {

    @NotNull
    private AuthCodeBlockData authCodeBlockData;

}
