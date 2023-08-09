package it.pagopa.swclient.mil.idpay.client.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class InitiativeDTO {
    private String initiativeId;
    private String initiativeName;
    private String organizationName;
    private InitiativeStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String serviceId;
    private Boolean enabled;

}
