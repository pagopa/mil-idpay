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
@ToString
@RegisterForReflection
public class CreateTransactionResp extends Transaction{

    @Size(min = 8, max = 16)
    private byte[] challenge;

    @NotNull
    @Pattern(regexp = "^[\u0001-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]{1,256}$")
    private String qrCode;
}
