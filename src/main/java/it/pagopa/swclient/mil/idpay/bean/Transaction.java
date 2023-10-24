package it.pagopa.swclient.mil.idpay.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"secondFactor"})
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private byte[] challenge;

    @NotNull
    @Pattern(regexp = "^[0-9a-zA-Z]{8}$")
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

    private String secondFactor;
}
