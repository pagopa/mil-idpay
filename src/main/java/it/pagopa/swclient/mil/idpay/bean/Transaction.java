package it.pagopa.swclient.mil.idpay.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"secondFactor"})
@RegisterForReflection
public class Transaction {

    @NotNull
    @Pattern(regexp = "^[\u0001-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]{1,256}$")
    private String idpayTransactionId;

    @NotNull
    @Pattern(regexp = "^[\u0001-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]{1,256}$")
    private String milTransactionId;

    @NotNull
    @Pattern(regexp = "^[\u0001-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]{1,256}$")
    private String initiativeId;

    @NotNull
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])T(2[0-3]|[01]\\d):[0-5]\\d:[0-5]\\d")
    private String timestamp;

    @NotNull
    @Min(value = 0L)
    @Max(value = 99999999999L)
    private Long goodsCost;

    @Size(min = 8, max = 16)
    private String challenge;

    @NotNull
    @Pattern(regexp = "^[\u0001-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]{1,256}$")
    private String trxCode;

    @NotNull
    @Pattern(regexp = "^[\u0001-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]{1,256}$")
    private String qrCode;

    @NotNull
    @Min(value = 0L)
    @Max(value = 99999999999L)
    private Long coveredAmount;

    @NotNull
    private TransactionStatus status;

    @NotNull
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])T(2[0-3]|[01]\\d):[0-5]\\d:[0-5]\\d")
    private String lastUpdate;

    @Pattern(regexp = "^[a-fA-F0-9]{12}$")
    private String secondFactor;
}
