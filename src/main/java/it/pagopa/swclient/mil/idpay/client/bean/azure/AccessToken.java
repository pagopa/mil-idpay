package it.pagopa.swclient.mil.idpay.client.bean.azure;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"access_token"})
public class AccessToken {
    private String token_type;
    private int expires_in;
    private int ext_expires_in;
    private String access_token;
}
