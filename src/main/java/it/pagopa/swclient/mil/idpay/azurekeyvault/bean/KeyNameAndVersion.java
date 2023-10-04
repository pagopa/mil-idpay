package it.pagopa.swclient.mil.idpay.azurekeyvault.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Antonio Tarricone
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class KeyNameAndVersion {
    /*
     *
     */
    private String name;

    /*
     *
     */
    private String version;
}