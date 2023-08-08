package it.pagopa.swclient.mil.idpay.dao;

import io.quarkus.runtime.annotations.RegisterForReflection;
import it.pagopa.swclient.mil.idpay.bean.TransactionStatus;

@RegisterForReflection
public class IdpayTransaction {

    private String acquirerId;
    private String channel;
    private String merchantId;
    private String terminalId;
    private String idpayTransactionId;
    private String milTransactionId;
    private String initiativeId;
    private String timestamp;
    private Long goodsCost;
    private String challenge;
    private String trxCode;
    private String qrCode;
    private Long coveredAmount;
    private TransactionStatus status;
    private String lastUpdate;

    public String getAcquirerId() {
        return acquirerId;
    }

    public void setAcquirerId(String acquirerId) {
        this.acquirerId = acquirerId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getIdpayTransactionId() {
        return idpayTransactionId;
    }

    public void setIdpayTransactionId(String idpayTransactionId) {
        this.idpayTransactionId = idpayTransactionId;
    }

    public String getMilTransactionId() {
        return milTransactionId;
    }

    public void setMilTransactionId(String milTransactionId) {
        this.milTransactionId = milTransactionId;
    }

    public String getInitiativeId() {
        return initiativeId;
    }

    public void setInitiativeId(String initiativeId) {
        this.initiativeId = initiativeId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Long getGoodsCost() {
        return goodsCost;
    }

    public void setGoodsCost(Long goodsCost) {
        this.goodsCost = goodsCost;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getTrxCode() {
        return trxCode;
    }

    public void setTrxCode(String trxCode) {
        this.trxCode = trxCode;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public Long getCoveredAmount() {
        return coveredAmount;
    }

    public void setCoveredAmount(Long coveredAmount) {
        this.coveredAmount = coveredAmount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return "IdpayTransaction{" +
                "acquirerId='" + acquirerId + '\'' +
                ", channel='" + channel + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", terminalId='" + terminalId + '\'' +
                ", idpayTransactionId='" + idpayTransactionId + '\'' +
                ", milTransactionId='" + milTransactionId + '\'' +
                ", initiativeId='" + initiativeId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", goodsCost=" + goodsCost +
                ", challenge='" + challenge + '\'' +
                ", trxCode='" + trxCode + '\'' +
                ", qrCode='" + qrCode + '\'' +
                ", coveredAmount=" + coveredAmount +
                ", status=" + status +
                ", lastUpdate='" + lastUpdate + '\'' +
                '}';
    }
}
