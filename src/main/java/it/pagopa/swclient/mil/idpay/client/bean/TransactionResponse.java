package it.pagopa.swclient.mil.idpay.client.bean;

import it.pagopa.swclient.mil.idpay.bean.TransactionStatus;

import java.math.BigDecimal;
import java.util.Date;

public class TransactionResponse {

    private String id;
    private String trxCode;
    private String initiativeId;
    private String merchantId;
    private String idTrxAcquirer;
    private Date trxDate;
    private BigDecimal trxExpirationMinutes;
    private Long amountCents;
    private String amountCurrency;
    private String mcc;
    private String acquirerId;
    private TransactionStatus status;
    private String merchantFiscalCode;
    private String vat;
    private Boolean splitPayment;
    private Long residualAmountCents;
    private String qrcodePngUrl;
    private String qrcodeTxtUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrxCode() {
        return trxCode;
    }

    public void setTrxCode(String trxCode) {
        this.trxCode = trxCode;
    }

    public String getInitiativeId() {
        return initiativeId;
    }

    public void setInitiativeId(String initiativeId) {
        this.initiativeId = initiativeId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getIdTrxAcquirer() {
        return idTrxAcquirer;
    }

    public void setIdTrxAcquirer(String idTrxAcquirer) {
        this.idTrxAcquirer = idTrxAcquirer;
    }

    public Date getTrxDate() {
        return trxDate;
    }

    public void setTrxDate(Date trxDate) {
        this.trxDate = trxDate;
    }

    public BigDecimal getTrxExpirationMinutes() {
        return trxExpirationMinutes;
    }

    public void setTrxExpirationMinutes(BigDecimal trxExpirationMinutes) {
        this.trxExpirationMinutes = trxExpirationMinutes;
    }

    public Long getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(Long amountCents) {
        this.amountCents = amountCents;
    }

    public String getAmountCurrency() {
        return amountCurrency;
    }

    public void setAmountCurrency(String amountCurrency) {
        this.amountCurrency = amountCurrency;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getAcquirerId() {
        return acquirerId;
    }

    public void setAcquirerId(String acquirerId) {
        this.acquirerId = acquirerId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getMerchantFiscalCode() {
        return merchantFiscalCode;
    }

    public void setMerchantFiscalCode(String merchantFiscalCode) {
        this.merchantFiscalCode = merchantFiscalCode;
    }

    public String getVat() {
        return vat;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }

    public Boolean getSplitPayment() {
        return splitPayment;
    }

    public void setSplitPayment(Boolean splitPayment) {
        this.splitPayment = splitPayment;
    }

    public Long getResidualAmountCents() {
        return residualAmountCents;
    }

    public void setResidualAmountCents(Long residualAmountCents) {
        this.residualAmountCents = residualAmountCents;
    }

    public String getQrcodePngUrl() {
        return qrcodePngUrl;
    }

    public void setQrcodePngUrl(String qrcodePngUrl) {
        this.qrcodePngUrl = qrcodePngUrl;
    }

    public String getQrcodeTxtUrl() {
        return qrcodeTxtUrl;
    }

    public void setQrcodeTxtUrl(String qrcodeTxtUrl) {
        this.qrcodeTxtUrl = qrcodeTxtUrl;
    }

    @Override
    public String toString() {
        return "TransactionResponse{" +
                "id='" + id + '\'' +
                ", trxCode='" + trxCode + '\'' +
                ", initiativeId='" + initiativeId + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", idTrxAcquirer='" + idTrxAcquirer + '\'' +
                ", trxDate=" + trxDate +
                ", trxExpirationMinutes=" + trxExpirationMinutes +
                ", amountCents=" + amountCents +
                ", amountCurrency='" + amountCurrency + '\'' +
                ", mcc='" + mcc + '\'' +
                ", acquirerId='" + acquirerId + '\'' +
                ", status=" + status +
                ", merchantFiscalCode='" + merchantFiscalCode + '\'' +
                ", vat='" + vat + '\'' +
                ", splitPayment=" + splitPayment +
                ", residualAmountCents=" + residualAmountCents +
                ", qrcodePngUrl='" + qrcodePngUrl + '\'' +
                ", qrcodeTxtUrl='" + qrcodeTxtUrl + '\'' +
                '}';
    }
}
