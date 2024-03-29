package it.pagopa.swclient.mil.idpay;

public final class ErrorCode {

    public static final String MODULE_ID = "00A";

    /*
     * Error codes.
     */

    // validation errors
    public static final String ERROR_INITIATIVEID_MUST_NOT_BE_NULL 							            = MODULE_ID + "000001";
    public static final String ERROR_TIMESTAMP_MUST_NOT_BE_NULL 							            = MODULE_ID + "000002";
    public static final String ERROR_TIMESTAMP_MUST_MATCH_REGEXP 							            = MODULE_ID + "000003";
    public static final String ERROR_GOODSCOST_MUST_NOT_BE_NULL 							            = MODULE_ID + "000004";
    public static final String ERROR_GOODSCOST_MUST_BE_GREATER_THAN 							        = MODULE_ID + "000005";
    public static final String ERROR_GOODSCOST_MUST_BE_LESS_THAN 							            = MODULE_ID + "000006";
    public static final String ERROR_TRANSACTION_ID_MUST_MATCH_REGEXP 							        = MODULE_ID + "000007";
    public static final String ERROR_KID_MUST_NOT_BE_NULL 							                    = MODULE_ID + "000008";
    public static final String ERROR_ENCSESSIONKEY_MUST_NOT_BE_NULL 							        = MODULE_ID + "000009";
    public static final String ERROR_AUTHCODEBLOCK_MUST_NOT_BE_NULL 							        = MODULE_ID + "000010";
    public static final String CIE_PUBLIC_KEY_MUST_NOT_BE_NULL 							                = MODULE_ID + "000011";
    public static final String SIGNATURE_MUST_NOT_BE_NULL 							                    = MODULE_ID + "000012";
    public static final String SOD_MUST_NOT_BE_NULL 							                        = MODULE_ID + "000013";
    public static final String AUTH_CODE_BLOCK_DATA_MUST_NOT_BE_NULL                                    = MODULE_ID + "000014";
    public static final String AUTHORIZE_TRANSACTION_MUST_NOT_BE_EMPTY                                  = MODULE_ID + "000015";
    public static final String ERROR_NIS_MUST_MATCH_REGEXP                                              = MODULE_ID + "000016";
    public static final String ERROR_CIE_PUBLIC_KEY_MUST_BE_BETWEEN                                     = MODULE_ID + "000017";
    public static final String ERROR_SIGNATURE_MUST_BE_BETWEEN                                          = MODULE_ID + "000018";
    public static final String ERROR_SOD_MUST_BE_BETWEEN                                                = MODULE_ID + "000019";
    public static final String ERROR_KID_MUST_MATCH_REGEXP                                              = MODULE_ID + "000020";
    public static final String ERROR_INITIATIVEID_MUST_MATCH_REGEXP                                     = MODULE_ID + "000021";
    public static final String ERROR_ENCSESSIONKEY_MUST_BE_BETWEEN                                      = MODULE_ID + "000022";
    public static final String ERROR_AUTHCODEBLOCK_MUST_BE_BETWEEN                                      = MODULE_ID + "000023";
    public static final String ERROR_ENCRYPTEDPINBLOCK_MUST_NOT_BE_NULL 							    = MODULE_ID + "000024";
    public static final String ERROR_ENCRYPTEDKEY_MUST_NOT_BE_NULL 							            = MODULE_ID + "000025";

    // rest server validation errors
    public static final String CREATE_TRANSACTION_MUST_NOT_BE_EMPTY 							        = MODULE_ID + "000030";
    public static final String VERIFY_CIE_MUST_NOT_BE_EMPTY 							                = MODULE_ID + "000031";
    public static final String ERROR_ENCRYPTING_SESSION_KEY 							                = MODULE_ID + "000032";

    // integration errors idpay - rest client
    public static final String ERROR_CALLING_IDPAY_REST_SERVICES 							            = MODULE_ID + "000050";
    public static final String ERROR_NOT_FOUND_IDPAY_REST_SERVICES 							            = MODULE_ID + "000051";
    public static final String ERROR_CALLING_AUTHORIZE_REST_SERVICES 							        = MODULE_ID + "000052";
    public static final String ERROR_IDPAY_PAYMENT_INVALID_PINBLOCK           							= MODULE_ID + "000053";
    public static final String ERROR_RETRIEVING_SECOND_FACTOR	 							            = MODULE_ID + "000055";
    public static final String ERROR_PIN_CIE_CURRENT_USER	 							                = MODULE_ID + "000056";


    // db errors
    public static final String ERROR_STORING_DATA_IN_DB 									            = MODULE_ID + "000060";
    public static final String ERROR_RETRIEVING_DATA_FROM_DB	 							            = MODULE_ID + "000061";
    public static final String ERROR_TRANSACTION_NOT_FOUND_MIL_DB	 							        = MODULE_ID + "000062";
    public static final String ERROR_WRONG_TRANSACTION_STATUS_MIL_DB	 							    = MODULE_ID + "000063";

    // integration errors ipzs - rest client
    public static final String ERROR_CALLING_IPZS_REST_SERVICES 							            = MODULE_ID + "000070";
    public static final String ERROR_NOT_FOUND_IPZS_REST_SERVICES 							            = MODULE_ID + "000071";
    public static final String ERROR_LOST_IPZS_REST_SERVICES 							                = MODULE_ID + "000072";
    public static final String ERROR_STOLEN_IPZS_REST_SERVICES 							                = MODULE_ID + "000073";
    public static final String ERROR_EXPIRED_IPZS_REST_SERVICES 							            = MODULE_ID + "000074";

    // integration errors azure (AD e Key Vault) - rest clients
    public static final String ERROR_CALLING_AZUREAD_REST_SERVICES 							            = MODULE_ID + "000080";
    public static final String AZUREAD_ACCESS_TOKEN_IS_NULL                                             = MODULE_ID + "000081";
    public static final String ERROR_GENERATING_KEY_PAIR                                                = MODULE_ID + "000082";
    public static final String ERROR_RETRIEVING_KEY_PAIR                                                = MODULE_ID + "000083";
    public static final String ERROR_RETRIEVING_PUBLIC_KEY_IDPAY                                        = MODULE_ID + "000084";
    public static final String ERROR_RETRIEVING_CERT_FOR_IDPAY                                          = MODULE_ID + "000085";
    public static final String ERROR_RETRIEVING_SECRET_FOR_IDPAY                                        = MODULE_ID + "000086";
    public static final String ERROR_GENERATING_KEY_STORE                                             = MODULE_ID + "000087";
    public static final String ERROR_CERTIFICATE_EXPIRED                                                = MODULE_ID + "000088";


    /*
     * Error descriptions
     */
    public static final String ERROR_CALLING_IDPAY_REST_SERVICES_DESCR = "Error calling IdPay rest service";
    public static final String ERROR_NOT_FOUND_IDPAY_REST_SERVICES_DESCR = "IdPay rest service not found response";
    public static final String ERROR_STORING_DATA_IN_DB_DESCR = "Error storing data in DB";
    public static final String ERROR_RETRIEVING_DATA_FROM_DB_DESCR = "Error retrieving data from DB";
    public static final String ERROR_TRANSACTION_NOT_FOUND_MIL_DB_DESCR = "Transaction not found on mil DB";
    public static final String ERROR_WRONG_TRANSACTION_STATUS_MIL_DB_DESCR = "Transaction status wrong on mil DB";
    public static final String ERROR_CALLING_IPZS_REST_SERVICES_DESCR = "Error calling IPZS rest service";
    public static final String ERROR_NOT_FOUND_IPZS_REST_SERVICES_DESCR = "IPZS rest service not found response";
    public static final String ERROR_LOST_IPZS_REST_SERVICES_DESCR = "IPZS rest service LOST response";
    public static final String ERROR_STOLEN_IPZS_REST_SERVICES_DESCR = "IPZS rest service STOLEN response";
    public static final String ERROR_EXPIRED_IPZS_REST_SERVICES_DESCR = "IPZS rest service EXPIRED response";
    public static final String ERROR_CALLING_AZUREAD_REST_SERVICES_DESCR = "Error calling Azure AD rest service";
    public static final String AZUREAD_ACCESS_TOKEN_IS_NULL_DESCR = "Azure AD sent NULL access token";
    public static final String ERROR_GENERATING_KEY_PAIR_DESCR = "Azure KV key pair generation error";
    public static final String ERROR_RETRIEVING_KEY_PAIR_DESCR = "Azure KV key pair retrieving error";
    public static final String ERROR_CALLING_AUTHORIZE_REST_SERVICES_DESCR = "Error calling IdPay rest service with authorize transaction";
    public static final String ERROR_RETRIEVING_PUBLIC_KEY_IDPAY_DESCR = "IDPay public key retrieving error";
    public static final String ERROR_ENCRYPTING_SESSION_KEY_DESCR = "Error occurs during encrypting session key";
    public static final String ERROR_RETRIEVING_SECOND_FACTOR_DESCR = "Error retrieving second factor";
    public static final String ERROR_RETRIEVING_CERT_FOR_IDPAY_DESCR = "Azure KV certificate for idpay retrieving error";
    public static final String ERROR_RETRIEVING_SECRET_FOR_IDPAY_DESCR = "Azure KV secret for idpay retrieving error";
    public static final String ERROR_GENERATING_KEY_STORE_FOR_IDPAY_DESCR = "Error during private key generation for idpay";
    public static final String ERROR_CERTIFICATE_EXPIRED_DESCR = "Azure KV certificate has expired";
    public static final String ERROR_PIN_CIE_CURRENT_USER_DESCR = "Missing cie pin for the current user";


    /*
     * Error messages
     */
    public static final String ERROR_CALLING_IDPAY_REST_SERVICES_MSG = "[" + ERROR_CALLING_IDPAY_REST_SERVICES + "] " + ERROR_CALLING_IDPAY_REST_SERVICES_DESCR;
    public static final String ERROR_NOT_FOUND_IDPAY_REST_SERVICES_MSG = "[" + ERROR_NOT_FOUND_IDPAY_REST_SERVICES + "] " + ERROR_NOT_FOUND_IDPAY_REST_SERVICES_DESCR;
    public static final String ERROR_STORING_DATA_IN_DB_MSG = "[" + ERROR_STORING_DATA_IN_DB + "] " + ERROR_STORING_DATA_IN_DB_DESCR;
    public static final String ERROR_RETRIEVING_DATA_FROM_DB_MSG = "[" + ERROR_RETRIEVING_DATA_FROM_DB + "] " + ERROR_RETRIEVING_DATA_FROM_DB_DESCR;
    public static final String ERROR_TRANSACTION_NOT_FOUND_MIL_DB_MSG = "[" + ERROR_TRANSACTION_NOT_FOUND_MIL_DB + "] " + ERROR_TRANSACTION_NOT_FOUND_MIL_DB_DESCR;
    public static final String ERROR_WRONG_TRANSACTION_STATUS_MIL_DB_MSG = "[" + ERROR_WRONG_TRANSACTION_STATUS_MIL_DB + "] " + ERROR_WRONG_TRANSACTION_STATUS_MIL_DB_DESCR;
    public static final String ERROR_CALLING_IPZS_REST_SERVICES_MSG = "[" + ERROR_CALLING_IPZS_REST_SERVICES + "] " + ERROR_CALLING_IPZS_REST_SERVICES_DESCR;
    public static final String ERROR_NOT_FOUND_IPZS_REST_SERVICES_MSG = "[" + ERROR_NOT_FOUND_IPZS_REST_SERVICES + "] " + ERROR_NOT_FOUND_IPZS_REST_SERVICES_DESCR;
    public static final String ERROR_LOST_IPZS_REST_SERVICES_MSG = "[" + ERROR_LOST_IPZS_REST_SERVICES + "] " + ERROR_LOST_IPZS_REST_SERVICES_DESCR;
    public static final String ERROR_STOLEN_IPZS_REST_SERVICES_MSG = "[" + ERROR_STOLEN_IPZS_REST_SERVICES + "] " + ERROR_STOLEN_IPZS_REST_SERVICES_DESCR;
    public static final String ERROR_EXPIRED_IPZS_REST_SERVICES_MSG = "[" + ERROR_EXPIRED_IPZS_REST_SERVICES + "] " + ERROR_EXPIRED_IPZS_REST_SERVICES_DESCR;
    public static final String ERROR_CALLING_AZUREAD_REST_SERVICES_MSG = "[" + ERROR_CALLING_AZUREAD_REST_SERVICES + "] " + ERROR_CALLING_AZUREAD_REST_SERVICES_DESCR;
    public static final String AZUREAD_ACCESS_TOKEN_IS_NULL_MSG = "[" + AZUREAD_ACCESS_TOKEN_IS_NULL + "] " + AZUREAD_ACCESS_TOKEN_IS_NULL_DESCR;
    public static final String ERROR_GENERATING_KEY_PAIR_MSG = "[" + ERROR_GENERATING_KEY_PAIR + "] " + ERROR_GENERATING_KEY_PAIR_DESCR;
    public static final String ERROR_RETRIEVING_KEY_PAIR_MSG = "[" + ERROR_RETRIEVING_KEY_PAIR + "] " + ERROR_RETRIEVING_KEY_PAIR_DESCR;
    public static final String ERROR_CALLING_AUTHORIZE_REST_SERVICES_MSG = "[" + ERROR_CALLING_AUTHORIZE_REST_SERVICES + "] " + ERROR_CALLING_AUTHORIZE_REST_SERVICES_DESCR;
    public static final String ERROR_RETRIEVING_PUBLIC_KEY_IDPAY_MSG = "[" + ERROR_RETRIEVING_PUBLIC_KEY_IDPAY + "] " + ERROR_RETRIEVING_PUBLIC_KEY_IDPAY_DESCR;
    public static final String ERROR_ENCRYPTING_SESSION_KEY_MSG = "[" + ERROR_ENCRYPTING_SESSION_KEY + "] " + ERROR_ENCRYPTING_SESSION_KEY_DESCR;
    public static final String ERROR_RETRIEVING_SECOND_FACTOR_MSG = "[" + ERROR_RETRIEVING_SECOND_FACTOR + "] " + ERROR_RETRIEVING_SECOND_FACTOR_DESCR;
    public static final String ERROR_RETRIEVING_CERT_FOR_IDPAY_MSG = "[" + ERROR_RETRIEVING_CERT_FOR_IDPAY + "] " + ERROR_RETRIEVING_CERT_FOR_IDPAY_DESCR;
    public static final String ERROR_RETRIEVING_SECRET_FOR_IDPAY_MSG = "[" + ERROR_RETRIEVING_SECRET_FOR_IDPAY + "] " + ERROR_RETRIEVING_SECRET_FOR_IDPAY_DESCR;
    public static final String ERROR_GENERATING_KEY_STORE_MSG = "[" + ERROR_GENERATING_KEY_STORE + "] " + ERROR_GENERATING_KEY_STORE_FOR_IDPAY_DESCR;
    public static final String ERROR_CERTIFICATE_EXPIRED_MSG = "[" + ERROR_CERTIFICATE_EXPIRED + "] " + ERROR_CERTIFICATE_EXPIRED_DESCR;
    public static final String ERROR_PIN_CIE_CURRENT_USER_MSG = "[" + ERROR_PIN_CIE_CURRENT_USER + "] " + ERROR_PIN_CIE_CURRENT_USER_DESCR;

    private ErrorCode() {
    }
}
