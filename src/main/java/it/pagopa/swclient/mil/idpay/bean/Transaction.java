package it.pagopa.swclient.mil.idpay.bean;

import jakarta.validation.constraints.*;

public class Transaction {

    @NotNull
    @Pattern(regexp = "^[\u0001-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]{1,256}$")
    private String idpayTransactionId;

    @NotNull
    @Pattern(regexp = "^[\u0001-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]{1,256}$")
    private String milTransactionId;

    @NotNull
    @Pattern(regexp = "^[\u0001-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]{1,256}$")
    private String initiativeId;

    @NotNull
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])T(2[0-3]|[01]\\d):[0-5]\\d:[0-5]\\d")
    private String timestamp;

    @NotNull
    @Min(value = 0L)
    @Max(value = 99999999999L)
    private Long goodsCost;

    @Size(min = 8, max = 16)
    private String challenge;

    @NotNull
    @Pattern(regexp = "^[\u0001-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]{1,256}$")
    private String trxCode;

    @NotNull
    @Pattern(regexp = "^[\u0001-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]{1,256}$")
    private String qrCode;

    @NotNull
    @Min(value = 0L)
    @Max(value = 99999999999L)
    private Long coveredAmount;

    @NotNull
    private TransactionStatus status;

    @NotNull
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])T(2[0-3]|[01]\\d):[0-5]\\d:[0-5]\\d")
    private String lastUpdate;

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
        return "Transaction{" +
                "idpayTransactionId='" + idpayTransactionId + '\'' +
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
