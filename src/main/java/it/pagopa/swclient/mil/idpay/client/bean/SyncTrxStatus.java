package it.pagopa.swclient.mil.idpay.client.bean;

import it.pagopa.swclient.mil.idpay.bean.OperationType;
import it.pagopa.swclient.mil.idpay.bean.TransactionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"secondFactor"})
public class SyncTrxStatus {

    private String id;
    private String idTrxIssuer;
    private String trxCode;
    private Date trxDate;
    private Date authDate;
    private OperationType operationType;
    private Long amountCents;
    private String amountCurrency;
    private String mcc;
    private String acquirerId;
    private String merchantId;
    private String initiativeId;
    private Long rewardCents;
    private List<String> rejectionReasons;
    private TransactionStatus status;
    private String secondFactor;
}
