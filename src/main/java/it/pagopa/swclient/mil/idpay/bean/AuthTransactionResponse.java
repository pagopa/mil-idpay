package it.pagopa.swclient.mil.idpay.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTransactionResponse {

    private AuthTransactionResponseOk authTransactionResponseOk;
    private AuthTransactionResponseWrong authTransactionResponseWrong;
}
