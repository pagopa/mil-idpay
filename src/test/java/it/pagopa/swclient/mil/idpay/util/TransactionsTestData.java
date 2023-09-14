package it.pagopa.swclient.mil.idpay.util;

import it.pagopa.swclient.mil.idpay.bean.CreateTransaction;
import it.pagopa.swclient.mil.idpay.bean.OperationType;
import it.pagopa.swclient.mil.idpay.bean.TransactionStatus;
import it.pagopa.swclient.mil.idpay.client.bean.SyncTrxStatus;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionResponse;
import it.pagopa.swclient.mil.idpay.client.bean.azure.AccessToken;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.IpzsVerifyCieResponse;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.Outcome;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransaction;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionEntity;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public final class TransactionsTestData {

    private static SimpleDateFormat lastUpdateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

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
}
