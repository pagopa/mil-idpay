package it.pagopa.swclient.mil.idpay;

public final class ErrorCode {

    public static final String MODULE_ID = "009";

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

    // rest server validation errors
    public static final String CREATE_TRANSACTION_MUST_NOT_BE_EMPTY 							        = MODULE_ID + "000030";

    // integration errors idpay - rest client
    public static final String ERROR_CALLING_IDPAY_REST_SERVICES 							            = MODULE_ID + "000050";
    public static final String ERROR_NOT_FOUND_IDPAY_REST_SERVICES 							            = MODULE_ID + "000051";


    // db errors
    public static final String ERROR_STORING_DATA_IN_DB 									            = MODULE_ID + "000060";
    public static final String ERROR_RETRIEVING_DATA_FROM_DB	 							            = MODULE_ID + "000061";
    public static final String ERROR_TRANSACTION_NOT_FOUND_MIL_DB	 							        = MODULE_ID + "000062";

    /*
     * Error descriptions
     */
    public static final String ERROR_CALLING_IDPAY_REST_SERVICES_DESCR = "Error calling IdPay rest service";
    public static final String ERROR_NOT_FOUND_IDPAY_REST_SERVICES_DESCR = "IdPay rest service not found response";
    public static final String ERROR_STORING_DATA_IN_DB_DESCR = "Error storing data in DB";
    public static final String ERROR_RETRIEVING_DATA_FROM_DB_DESCR = "Error retrieving data from DB";
    public static final String ERROR_TRANSACTION_NOT_FOUND_MIL_DB_DESCR = "Transaction not found on mil DB";


    /*
     * Error messages
     */
    public static final String ERROR_CALLING_IDPAY_REST_SERVICES_MSG = "[" + ERROR_CALLING_IDPAY_REST_SERVICES + "] " + ERROR_CALLING_IDPAY_REST_SERVICES_DESCR;
    public static final String ERROR_NOT_FOUND_IDPAY_REST_SERVICES_MSG = "[" + ERROR_NOT_FOUND_IDPAY_REST_SERVICES + "] " + ERROR_NOT_FOUND_IDPAY_REST_SERVICES_DESCR;
    public static final String ERROR_STORING_DATA_IN_DB_MSG = "[" + ERROR_STORING_DATA_IN_DB + "] " + ERROR_STORING_DATA_IN_DB_DESCR;
    public static final String ERROR_RETRIEVING_DATA_FROM_DB_MSG = "[" + ERROR_RETRIEVING_DATA_FROM_DB + "] " + ERROR_RETRIEVING_DATA_FROM_DB_DESCR;
    public static final String ERROR_TRANSACTION_NOT_FOUND_MIL_DB_MSG = "[" + ERROR_TRANSACTION_NOT_FOUND_MIL_DB + "] " + ERROR_TRANSACTION_NOT_FOUND_MIL_DB_DESCR;

    private ErrorCode() {
    }
}
