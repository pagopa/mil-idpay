package it.pagopa.swclient.mil.idpay.dao;

import io.quarkus.runtime.annotations.RegisterForReflection;
import it.pagopa.swclient.mil.idpay.bean.TransactionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@RegisterForReflection
public class IdpayTransaction {

    private String acquirerId;
    private String channel;
    private String merchantId;
    private String terminalId;
    private String idpayTransactionId;
    private String milTransactionId;
    private String initiativeId;
    private String timestamp;
    private Long goodsCost;
    private String challenge;
    private String trxCode;
    private String qrCode;
    private Long coveredAmount;
    private TransactionStatus status;
    private String lastUpdate;


    @Override
    public String toString() {
        return "IdpayTransaction{" +
                "acquirerId='" + acquirerId + '\'' +
                ", channel='" + channel + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", terminalId='" + terminalId + '\'' +
                ", idpayTransactionId='" + idpayTransactionId + '\'' +
                ", milTransactionId='" + milTransactionId + '\'' +
                ", initiativeId='" + initiativeId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", goodsCost=" + goodsCost +
                ", challenge='" + challenge + '\'' +
                ", trxCode='" + trxCode + '\'' +
                ", qrCode='" + qrCode + '\'' +
                ", coveredAmount=" + coveredAmount +
                ", status=" + status +
                ", lastUpdate='" + lastUpdate + '\'' +
                '}';
    }
}
