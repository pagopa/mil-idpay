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
public class CertificateBundle {

    @JsonProperty("id")
    public String id;

    @JsonProperty("kid")
    public String kid;

    @JsonProperty("sid")
    public String sid;

    @JsonProperty("x5t")
    public String x5t;

    @JsonProperty("cer")
    public byte[] cer;

    @JsonProperty("attributes")
    public Attributes attributes;

    @JsonProperty("policy")
    public Policy policy;
}
