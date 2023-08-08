package it.pagopa.swclient.mil.idpay.client.bean;

import java.time.LocalDate;

public class InitiativeDTO {
    private String initiativeId;
    private String initiativeName;
    private String organizationName;
    private InitiativeStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String serviceId;
    private Boolean enabled;

    public String getInitiativeId() {
        return initiativeId;
    }

    public void setInitiativeId(String initiativeId) {
        this.initiativeId = initiativeId;
    }

    public String getInitiativeName() {
        return initiativeName;
    }

    public void setInitiativeName(String initiativeName) {
        this.initiativeName = initiativeName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public InitiativeStatus getStatus() {
        return status;
    }

    public void setStatus(InitiativeStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "InitiativeDTO{" +
                "initiativeId='" + initiativeId + '\'' +
                ", initiativeName='" + initiativeName + '\'' +
                ", organizationName='" + organizationName + '\'' +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", serviceId='" + serviceId + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
