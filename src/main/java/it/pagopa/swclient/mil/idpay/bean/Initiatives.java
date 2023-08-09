package it.pagopa.swclient.mil.idpay.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Initiatives {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Initiative> initiatives;

}
