package it.pagopa.swclient.mil.idpay.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class AuthTransactionResponse {

    private String id;
    private String trxCode;
    private Date trxDate;
    private String initiativeId;
    private String initiativeName;
    private String businessName;
    private TransactionStatus status;
    private Long rewardCents;
    private Long amountCents;
    private Long residualBudgetCents;
}
