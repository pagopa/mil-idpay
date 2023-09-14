package it.pagopa.swclient.mil.idpay.dao;

import io.quarkus.runtime.annotations.RegisterForReflection;
import it.pagopa.swclient.mil.idpay.bean.TransactionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@RegisterForReflection
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
    private String challenge;
    private String trxCode;
    private String qrCode;
    private Long coveredAmount;
    private TransactionStatus status;
    private String lastUpdate;

}
