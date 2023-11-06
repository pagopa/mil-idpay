package it.pagopa.swclient.mil.idpay.client.bean.azure;

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
public class AccessToken {
    @JsonProperty("token_type")
    private String type;

    @JsonProperty("expires_on")
    private long expiresOn;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("resource")
    private String resource;

    @JsonProperty("access_token")
    private String token;
}
