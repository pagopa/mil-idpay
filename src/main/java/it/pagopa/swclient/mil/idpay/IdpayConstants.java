package it.pagopa.swclient.mil.idpay;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IdpayConstants {

    public static final String TRANSACTION_ID_REGEX = "^[\u0001-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]{1,256}$";
}
