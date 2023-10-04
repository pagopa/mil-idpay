package it.pagopa.swclient.mil.idpay.azurekeyvault.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

@RegisterForReflection
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class UnwrapKeyRequest {

    @JsonProperty("alg")
    private String alg;

    @JsonProperty("value")
    private String value;
}
