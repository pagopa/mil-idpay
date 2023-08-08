package it.pagopa.swclient.mil.idpay.client.bean;

public class TransactionCreationRequest {

    private String initiativeId;
    private Long amountCents;
    private String mcc;
    private String idTrxAcquirer;

    public String getInitiativeId() {
        return initiativeId;
    }

    public void setInitiativeId(String initiativeId) {
        this.initiativeId = initiativeId;
    }

    public Long getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(Long amountCents) {
        this.amountCents = amountCents;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getIdTrxAcquirer() {
        return idTrxAcquirer;
    }

    public void setIdTrxAcquirer(String idTrxAcquirer) {
        this.idTrxAcquirer = idTrxAcquirer;
    }

    @Override
    public String toString() {
        return "TransactionCreationRequest{" +
                "initiativeId='" + initiativeId + '\'' +
                ", amountCents=" + amountCents +
                ", mcc='" + mcc + '\'' +
                ", idTrxAcquirer='" + idTrxAcquirer + '\'' +
                '}';
    }
}
