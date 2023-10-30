package it.pagopa.swclient.mil.idpay.client.bean;

import it.pagopa.swclient.mil.idpay.bean.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"secondFactor"})
public class PreAuthPaymentResponseDTO {

    private String id;
    private String trxCode;
    private Date trxDate;
    private String initiativeId;
    private String initiativeName;
    private String businessName;
    private TransactionStatus status;
    private Long reward;
    private Long amountCents;
    private BigDecimal residualBudget;
    private String secondFactor;
}
