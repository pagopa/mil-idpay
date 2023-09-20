package it.pagopa.swclient.mil.idpay.util;

import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.*;
import it.pagopa.swclient.mil.idpay.bean.CreateTransaction;
import it.pagopa.swclient.mil.idpay.bean.OperationType;
import it.pagopa.swclient.mil.idpay.bean.TransactionStatus;
import it.pagopa.swclient.mil.idpay.bean.VerifyCie;
import it.pagopa.swclient.mil.idpay.client.bean.SyncTrxStatus;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionResponse;
import it.pagopa.swclient.mil.idpay.client.bean.azure.AccessToken;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.IpzsVerifyCieResponse;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.Outcome;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransaction;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionEntity;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public final class TransactionsTestData {

    //valore vaultUrl deve coincidere con valore del parametro %test.quarkus.rest-client.azure-key-vault-api.url del application.properties
    private static final String vaultUrl = "https://156360cd-f617-4dcb-b908-ae29a2a8651c.mock.pstmn.io";
    private static SimpleDateFormat lastUpdateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static final String KEY_NAME = "0709643f49394529b92c19a68c8e184a";
    private static final String KEY_VERSION = "6581c704deda4979943c3b34468df7c2";
    //private static final String KID = KEY_NAME + "/" + KEY_VERSION;
    private static final String KEY_RECOVERY_LEVEL = "Purgeable";
    private static final String KEY_TYPE = "RSA";
    private static final String[] KEY_OPS = new String[] {
            "wrapKey", "unwrapKey"
    };
    private static final String MODULUS = "AKnFsF5Y16TB9qkmoOyDXG3ulenUWYoW78U7mcGBoYKRpMlswxhc_ZiKcC65vIrCP6hbS5Cx88IbQG2DWH-nE329OLzUbzcdraDLR-7V2BX0nNwmwXxhkd4ofzzjKyhWjV8AkxFpqJPtFG09YCyCpaC8YluVPbHUpWJ1wrOsavdc_YM1W1XuaGvJv4SkilM8vBa81zOLEVhbEE5msHxPNLwVyC_0PIE6OFL9RY4YP1U1q7gjTMmKDc9qgEYkdziMnlxWp_EkKTZOERbEatP0fditFt-zWKlXw0qO4FKFlmj9n5tbB55vaopB71Kv6LcsAY1Q-fgOuoM41HldLppzfDOPwLGyCQF9ODJt1xaKkup6i_BxZum7-QckibwPaj3ODZbYsPuNZ_npQiR6NJZ_q_31YMlyuGdqltawluYLJidw3EzkpTN__bHdio892WbY29PRwbrG486IJ_88qP3lWs1TfzohVa1czUOZwQHqp0ixVBi_SK3jICk-V65DbwzgS5zwBFaqfWO3XVOf6tmWFMZ6ly7wtOnYWoMR15rudsD5xXWwqE-s7IP1lVZuIOdMfLH7-1Pgn-YJuPsBLbZri9_M4KtflYbqnuDckSyFNBynTwoSvSSuBhpkmNgiSQ-WBXHHss5Wy-pr-YjNK7JYppPOHvfHSY96XnJl9SPWcnwx";
    //private static final String PRIVATE_EXPONENT = "IlITaUNTFtzaUVA8lIuqxhOHLW3vCv4_ixMVLnwXC0cHteudliGIZ8vGyX9laPTDezS3lkEPSuSI9gqpO6cqRs9Xtr7IW-9NQDYQLO2AoVGh21SfZVZxL2Tm8gdnnGBA9J1wXcMLIBp7uGjBtkXUF2Y2CRcm0XowU_MEASAgQLEFE_8Xn4vSgsXWiIld6F1dFcinxaT9xOul5H-Yeozll4dcwKsCh0pehBJs-wCWXxK6S_-g4JZe29lHJMbu7hjpU7f1_AcIKNEH3d8nzID-5ux49RCz4goasgonua8FXOS23Sh-Jg6WjmwtZj0nEc6c4rVlzzqlBG2a8I0ApJsnlo2RK1E-XftVNip52Bsb9jRKGNjNZP3VOgAdLg-py8HVU3sxn95yJRN6AF7S8a0Jnb6uAzxagmfZqLe1ykswBPJWPP2dyQivb59CMcmHQoOK-up_Tt1P6oIltTCHEg0z79GVatWvikmfrN0tLrMJl8iR_67IDvehkp0r4DoFQNkhKNm5moFGFJWqkWZSpi3OUhPYZNmWPJTf1CxM3li6hNqRuGLCe-M9-gyZ01U9j9sUbV3xaK6kXhDPje2JB-0FkZuU7ewmpmQ5ETuRYrXyQa6b6VyxNwYokvgAGxdQ8leT2jxq_UVoMw-C0JU8tOC1fkXxClfOsSfCKx5WQXIKFrU=";
    private static final String PUBLIC_EXPONENT = "AQAB";

    public static Map<String, String> getMilHeaders() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("RequestId", UUID.randomUUID().toString());
        headerMap.put("Version", "1.0.0");
        headerMap.put("AcquirerId", "4585625");
        headerMap.put("Channel", "POS");
        headerMap.put("TerminalId", "0aB9wXyZ");
        headerMap.put("MerchantId", "28405fHfk73x88D");
        headerMap.put("SessionId", UUID.randomUUID().toString());
        return headerMap;
    }

    public static TransactionResponse getCreatedTransaction() {

        TransactionResponse res = new TransactionResponse();

        res.setId("4490eea8-9c81-4879-9720-22222222333");
        res.setTrxCode("trxCodetrxCodetrxCode");
        res.setInitiativeId("initiativeId1");
        res.setMerchantId("111");
        res.setIdTrxAcquirer("4490eea8-9c81-4879-9720-9578de10ff4e");
        res.setTrxDate(new Date());
        res.setTrxExpirationMinutes(new BigDecimal(14235426));
        res.setAmountCents(99999999999L);
        res.setAmountCurrency("EUR");
        res.setMcc("mcc1");
        res.setAcquirerId("222");
        res.setStatus(TransactionStatus.CREATED);
        res.setMerchantFiscalCode("112331234");
        res.setVat("vat1");
        res.setSplitPayment(false);
        res.setResidualAmountCents(0L);
        res.setQrcodePngUrl("qrcodePngUrl");
        res.setQrcodeTxtUrl("qrcodeTxtUrl");

        return res;
    }

    public static CreateTransaction getCreateTransactionRequest() {

        CreateTransaction res = new CreateTransaction();

        res.setInitiativeId("aaaaaaaaaaaaaaaaaa");
        res.setTimestamp("2023-07-30T14:27:38");
        res.setGoodsCost(333L);

        return res;
    }

    public static IdpayTransactionEntity getTransactionEntity(Map<String, String> headers, CreateTransaction createTransaction, TransactionResponse res) {
        IdpayTransaction idpayTransaction = new IdpayTransaction();

        idpayTransaction.setAcquirerId(headers.get("AcquirerId"));
        idpayTransaction.setChannel(headers.get("Channel"));
        idpayTransaction.setMerchantId(res.getMerchantId());
        idpayTransaction.setTerminalId(headers.get("TerminalId"));
        idpayTransaction.setIdpayTransactionId(res.getId());

        idpayTransaction.setMilTransactionId(res.getIdTrxAcquirer());

        idpayTransaction.setInitiativeId(res.getInitiativeId());
        idpayTransaction.setTimestamp(createTransaction.getTimestamp());
        idpayTransaction.setGoodsCost(createTransaction.getGoodsCost());
        idpayTransaction.setTrxCode(res.getTrxCode());
        idpayTransaction.setCoveredAmount(30L);
        idpayTransaction.setStatus(res.getStatus());
        idpayTransaction.setLastUpdate(lastUpdateFormat.format(new Date()));

        IdpayTransactionEntity entity = new IdpayTransactionEntity();


        entity.transactionId = res.getIdTrxAcquirer();

        entity.idpayTransaction = idpayTransaction;

        return entity;
    }

    public static SyncTrxStatus getStatusTransactionResponse() {

        SyncTrxStatus res = new SyncTrxStatus();

        res.setId("4490eea8-9c81-4879-9720-22222222333");
        res.setIdTrxIssuer("IdTrxIssuer1");
        res.setTrxCode("trxCodetrxCodetrxCode");
        res.setTrxDate(new Date());
        res.setAuthDate(new Date());
        res.setOperationType(OperationType.CHARGE);
        res.setAmountCents(99999999999L);
        res.setAmountCurrency("EUR");
        res.setMcc("mcc1");
        res.setAcquirerId("222");
        res.setMerchantId("111");
        res.setInitiativeId("initiativeId1");
        res.setRewardCents(123L);
        res.setStatus(TransactionStatus.CREATED);
        res.setSecondFactor("483efab359c1");

        return res;
    }

    public static IpzsVerifyCieResponse getIpzsVerifyCieResponseOK() {

        IpzsVerifyCieResponse res = new IpzsVerifyCieResponse();
        res.setOutcome(Outcome.OK);

        return res;
    }

    public static AccessToken getAzureADAccessToken() {

        AccessToken token = new AccessToken();

        token.setToken_type("Bearer");
        token.setExpires_in(900);
        token.setExt_expires_in(1800);
        token.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");

        return token;
    }

    public static DetailedKey getAzureKVGetKeyResponse() {
        long now = Instant.now().getEpochSecond();
        KeyAttributes keyAttributes = new KeyAttributes(now - 300, now + 600, now - 300, now - 300, Boolean.TRUE, KEY_RECOVERY_LEVEL, 0, Boolean.FALSE);

        KeyDetails details = new KeyDetails(vaultUrl + "/keys/" + KEY_NAME + "/" + KEY_VERSION, KEY_TYPE, KEY_OPS, MODULUS, PUBLIC_EXPONENT);

        return new DetailedKey(details, keyAttributes);
    }

    public static VerifyCie getVerifyCieRequest() {

        VerifyCie res = new VerifyCie();

        res.setNis("432543576456");
        res.setCiePublicKey("MEgCQQCo9+BpMRYQ/dL3DS2CyJxRF+j6ctbT3/Qp84+KeFhnii7NT7fELilKUSnxS30WAvQCCo2yU1orfgqr41mM70MBAgMBAAE=");
        res.setSignature("14OJ2TV93OT621XR0MV5NJtMOcK2nCyBXMRhpVxO88xgzhMpoMQ2fQs+ug8+8oKhsOwkYKYRW9VY" +
                "9u3oYaKIH75RJPHlVe+4J9gKZ6IqpUsYu3Mvb2PDrlwzZV5KH232/b/6QFoD7/STBtMOO8rU0lTk" +
                "HTw11VsCgHSW4Jv6N4P98zD0ScgIpvgcis5H0SkApyIZs/qAkJyAP9iEw7tOfbO3q1jkK+7nV9G9" +
                "ia/nlJl+itD6/Nv1u+KQ8OwFq5kPNaDsXPI1CBQp/Ll4Q7gVMWH5toGltfpouQZaMxukb2z0IWxV" +
                "C//gbIRBoW3qsxlBi23HMNaieyGal2cCEjC/Ng==");
        res.setSod("EkCTZfFjWVBAXh1ahqBJwKJYD1o7HKBGF0QOCkITt1mGNwQGMSBkDp/xMvyU" +
                "Lz3RQG07UYZDWUEZVsAUQ9LKTs6jz7JGgSDH1Yt3+b3PmAahbeLJLj77MFRH5mDHmTekZH3/db+g" +
                "fZElM8M0AcOAgfYYatA/F1S");

        return res;
    }

    public static DetailedKey getAzureKVCreateKeyResponse() {
        long now = Instant.now().getEpochSecond();
        KeyAttributes keyAttributes = new KeyAttributes(now - 300, now + 600, now - 300, now - 300, Boolean.TRUE, KEY_RECOVERY_LEVEL, 0, Boolean.FALSE);

        KeyDetails details = new KeyDetails(vaultUrl + "/keys/" + KEY_NAME + "/" + KEY_VERSION, KEY_TYPE, KEY_OPS, MODULUS, PUBLIC_EXPONENT);

        return new DetailedKey(details, keyAttributes);
    }

    public static DetailedKey getAzureKVGetKeyResponseAttrNull() {
        //long now = Instant.now().getEpochSecond();
        //KeyAttributes keyAttributes = new KeyAttributes(now - 300, now + 600, now - 300, now - 300, Boolean.TRUE, KEY_RECOVERY_LEVEL, 0, Boolean.FALSE);

        KeyDetails details = new KeyDetails(vaultUrl + "/keys/" + KEY_NAME + "/" + KEY_VERSION, KEY_TYPE, KEY_OPS, MODULUS, PUBLIC_EXPONENT);
        return new DetailedKey(details, null);
    }

    public static Map<String, String> getMilHeadersNonPOS() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("RequestId", UUID.randomUUID().toString());
        headerMap.put("Version", "1.0.0");
        headerMap.put("AcquirerId", "4585625");
        headerMap.put("Channel", "ATM");
        headerMap.put("TerminalId", "0aB9wXyZ");
        headerMap.put("MerchantId", "28405fHfk73x88D");
        headerMap.put("SessionId", UUID.randomUUID().toString());
        return headerMap;
    }

}
