package it.pagopa.swclient.mil.idpay.client.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class InitiativeDTO {
    private String initiativeId;
    private String initiativeName;
    private String organizationName;
    private InitiativeStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String serviceId;
    private Boolean enabled;

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
