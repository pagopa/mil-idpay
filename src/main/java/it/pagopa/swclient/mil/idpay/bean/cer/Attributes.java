package it.pagopa.swclient.mil.idpay.bean.cer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attributes {

    @JsonProperty("enabled")
    private Boolean enabled;

    @JsonProperty("nbf")
    private Long nbf;

    @JsonProperty("exp")
    private Long exp;

    @JsonProperty("created")
    private Long created;

    @JsonProperty("updated")
    private Long updated;

    @JsonProperty("recoveryLevel")
    private String recoveryLevel;
}
