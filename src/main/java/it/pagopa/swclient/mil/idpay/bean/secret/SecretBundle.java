package it.pagopa.swclient.mil.idpay.bean.secret;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import it.pagopa.swclient.mil.idpay.bean.cer.Attributes;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecretBundle {

    @JsonProperty("value")
    private String value;

    @JsonProperty("contentType")
    private String contentType;

    @JsonProperty("id")
    private String id;

    @JsonProperty("managed")
    private boolean managed;

    @JsonProperty("attributes")
    private Attributes attributes;

    @JsonProperty("kid")
    private String kid;
}
