package it.pagopa.swclient.mil.idpay.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class Initiatives {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Initiative> initiatives;

    public List<Initiative> getInitiatives() {
        return initiatives;
    }

    public void setInitiatives(List<Initiative> initiatives) {
        this.initiatives = initiatives;
    }

    @Override
    public String toString() {
        return "Initiatives{" +
                "initiatives=" + initiatives +
                '}';
    }
}
