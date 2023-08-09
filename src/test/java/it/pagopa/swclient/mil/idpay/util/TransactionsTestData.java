package it.pagopa.swclient.mil.idpay.util;

import it.pagopa.swclient.mil.idpay.bean.CreateTransaction;
import it.pagopa.swclient.mil.idpay.bean.TransactionStatus;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionResponse;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransaction;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionEntity;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        idpayTransaction.setChallenge(null);
        idpayTransaction.setTrxCode(res.getTrxCode());
        idpayTransaction.setQrCode(null);
        idpayTransaction.setCoveredAmount(null);
        idpayTransaction.setStatus(res.getStatus());
        idpayTransaction.setLastUpdate(lastUpdateFormat.format(new Date()));

        IdpayTransactionEntity entity = new IdpayTransactionEntity();


        entity.transactionId = res.getIdTrxAcquirer();

        entity.idpayTransaction = idpayTransaction;

        return entity;
    }
}
