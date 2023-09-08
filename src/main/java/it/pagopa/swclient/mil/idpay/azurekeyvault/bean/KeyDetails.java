/*
 * Key.java
 *
 * 23 lug 2023
 */
package it.pagopa.swclient.mil.idpay.azurekeyvault.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
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
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyDetails extends Key {
    /*
     *
     */
    @JsonProperty("kty")
    private String kty;

    /*
     *
     */
    @JsonProperty("key_ops")
    private String[] keyOps;

    /*
     * Modulus (Base64 URL-safe).
     */
    @JsonProperty("n")
    private String modulus;

    /*
     * Public exponent (Base64 URL-safe).
     */
    @JsonProperty("e")
    private String exponent;

    /**
     *
     * @param kid
     * @param kty
     * @param keyOps
     * @param modulus
     * @param exponent
     * @param attributes
     */
    public KeyDetails(String kid, String kty, String[] keyOps, String modulus, String exponent, KeyAttributes attributes) {
        super(kid, attributes);
        this.kty = kty;
        this.keyOps = keyOps;
        this.modulus = modulus;
        this.exponent = exponent;
    }
}