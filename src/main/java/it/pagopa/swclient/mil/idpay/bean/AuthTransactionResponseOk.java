package it.pagopa.swclient.mil.idpay.bean;

import lombok.Builder;
import lombok.Data;
import java.util.Date;

@Data
@Builder
public class AuthTransactionResponseOk {

    private String id;
    private String idTrxIssuer;
    private String trxCode;
    private Date trxDate;
    private OperationType operationType;
    private Long amountCents;
    private String amountCurrency;
    private String acquirerId;
    private String merchantId;
    private String initiativeId;
    private TransactionStatus status;
    private Long rewardCents;
}
