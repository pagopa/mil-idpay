package it.pagopa.swclient.mil.idpay.client.bean.ipzs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"nis"})
public class IpzsVerifyCieRequest {

    private String nis;
    private String sod;
    private String kpubint;
    private String challenge;
    private String challengeSignature;
}
