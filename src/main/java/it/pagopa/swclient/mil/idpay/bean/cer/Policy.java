package it.pagopa.swclient.mil.idpay.bean.cer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Policy {

    @JsonProperty("id")
    public String id;

    @JsonProperty("key_props")
    public KeyProps keyProps;

    @JsonProperty("secret_props")
    public SecretProps secretProps;

    @JsonProperty("x509_props")
    public X509Props x509Props;

    @JsonProperty("lifetime_actions")
    public List<LifetimeAction> lifetimeActions;

    @JsonProperty("issuer")
    public Issuer issuer;

    @JsonProperty("attributes")
    public Attributes attributes;
}
