package it.pagopa.swclient.mil.idpay.client.bean;

import it.pagopa.swclient.mil.idpay.bean.TransactionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TransactionResponse {

    private String id;
    private String trxCode;
    private String initiativeId;
    private String merchantId;
    private String idTrxAcquirer;
    private Date trxDate;
    private BigDecimal trxExpirationMinutes;
    private Long amountCents;
    private String amountCurrency;
    private String mcc;
    private String acquirerId;
    private TransactionStatus status;
    private String merchantFiscalCode;
    private String vat;
    private Boolean splitPayment;
    private Long residualAmountCents;
    private String qrcodePngUrl;
    private String qrcodeTxtUrl;

}
