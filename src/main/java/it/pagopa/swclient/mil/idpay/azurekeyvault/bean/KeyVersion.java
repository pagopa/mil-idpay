/*
 * KeyVersion.java
 *
 * 1 ago 2023
 */
package it.pagopa.swclient.mil.idpay.azurekeyvault.bean;


import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.NoArgsConstructor;

/**
 *
 * @author Antonio Tarricone
 */
@RegisterForReflection
@NoArgsConstructor
public class KeyVersion extends Key {
    /**
     *
     * @param kid
     * @param attributes
     */
    public KeyVersion(String kid, KeyAttributes attributes) {
        super(kid, attributes);
    }
}