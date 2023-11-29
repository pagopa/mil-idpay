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
public class X509Props {

    @JsonProperty("subject")
    public String subject;

    @JsonProperty("ekus")
    public List<String> ekus;

    @JsonProperty("key_usage")
    public List<Object> keyUsage;

    @JsonProperty("validity_months")
    public Integer validityMonths;
}
