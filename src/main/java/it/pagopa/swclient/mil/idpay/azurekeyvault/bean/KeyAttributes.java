/*
 * KeyAttributes.java
 *
 * 23 lug 2023
 */
package it.pagopa.swclient.mil.idpay.azurekeyvault.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Antonio Tarricone
 */
@RegisterForReflection
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyAttributes {
    /*
     * Creation epoch (seconds from 1/1/1970 00:00:00.000 GMT)
     */
    @JsonProperty("created")
    private Long created;

    /*
     * Expiration epoch (seconds from 1/1/1970 00:00:00.000 GMT)
     */
    @JsonProperty("exp")
    private Long exp;

    /*
     * 'Not before' epoch (seconds from 1/1/1970 00:00:00.000 GMT)
     */
    @JsonProperty("nbf")
    private Long nbf;

    /*
     *
     */
    @JsonProperty("updated")
    private Long updated;

    /*
     *
     */
    @JsonProperty("enabled")
    private Boolean enabled;

    /*
     *
     */
    @JsonProperty("recoveryLevel")
    private String recoveryLevel;

    /*
     *
     */
    @JsonProperty("recoverableDays")
    private Integer recoverableDays;

    /*
     *
     */
    @JsonProperty("exportable")
    private Boolean exportable;
}