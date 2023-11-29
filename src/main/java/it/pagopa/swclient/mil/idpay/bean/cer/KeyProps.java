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
public class KeyProps {

    @JsonProperty("exportable")
    private Boolean exportable;

    @JsonProperty("kty")
    private String kty;

    @JsonProperty("key_size")
    private Integer keySize;

    @JsonProperty("reuse_key")
    private Boolean reuseKey;
}
