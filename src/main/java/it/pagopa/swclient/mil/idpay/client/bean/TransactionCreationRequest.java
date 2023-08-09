package it.pagopa.swclient.mil.idpay.client.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TransactionCreationRequest {

    private String initiativeId;
    private Long amountCents;
    private String mcc;
    private String idTrxAcquirer;

}
