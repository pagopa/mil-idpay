package it.pagopa.swclient.mil.idpay.dao;

import io.quarkus.runtime.annotations.RegisterForReflection;
import it.pagopa.swclient.mil.idpay.bean.TransactionStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@RegisterForReflection
@EqualsAndHashCode(exclude = {"lastUpdate"})
public class IdpayTransaction {

    private String milTransactionId;
    private String acquirerId;
    private String channel;
    private String merchantId;
    private String terminalId;
    private String idpayMerchantId;
    private String idpayTransactionId;
    private String initiativeId;
    private String timestamp;
    private Long goodsCost;
    private String trxCode;
    private TransactionStatus status;
    private Long coveredAmount;
    private String lastUpdate;

}
