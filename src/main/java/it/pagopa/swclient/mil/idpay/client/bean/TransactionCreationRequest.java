package it.pagopa.swclient.mil.idpay.client.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionCreationRequest {

    private String initiativeId;
    private Long amountCents;
    private String mcc;
    private String idTrxAcquirer;

    @Override
    public String toString() {
        return "TransactionCreationRequest{" +
                "initiativeId='" + initiativeId + '\'' +
                ", amountCents=" + amountCents +
                ", mcc='" + mcc + '\'' +
                ", idTrxAcquirer='" + idTrxAcquirer + '\'' +
                '}';
    }
}
