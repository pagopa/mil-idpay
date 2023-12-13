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
    private String id;

    @JsonProperty("kid")
    private String kid;

    @JsonProperty("sid")
    private String sid;

    @JsonProperty("x5t")
    private String x5t;

    @JsonProperty("cer")
    private String cer;

    @JsonProperty("attributes")
    private Attributes attributes;

    @JsonProperty("policy")
    private Policy policy;
}
