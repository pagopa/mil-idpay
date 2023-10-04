package it.pagopa.swclient.mil.idpay.util;

import it.pagopa.swclient.mil.idpay.client.bean.InitiativeDTO;
import it.pagopa.swclient.mil.idpay.client.bean.InitiativeStatus;

import java.time.LocalDate;
import java.util.*;

public final class InitiativesTestData {

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

    public static List<InitiativeDTO> getMerchantInitiativeList() {

        InitiativeDTO initiative1 = new InitiativeDTO();
        initiative1.setInitiativeId("1a");
        initiative1.setInitiativeName("Iniziative name1");
        initiative1.setOrganizationName("Organization name1");
        initiative1.setStatus(InitiativeStatus.PUBLISHED);
        initiative1.setStartDate(LocalDate.of(2020, 1, 8));
        initiative1.setEndDate(LocalDate.of(2050, 3, 30));
        initiative1.setServiceId("serviceId1");
        initiative1.setEnabled(Boolean.TRUE);

        InitiativeDTO initiative2 = new InitiativeDTO();
        initiative2.setInitiativeId("2b");
        initiative2.setInitiativeName("Iniziative name2");
        initiative2.setOrganizationName("Organization name2");
        initiative2.setStatus(InitiativeStatus.PUBLISHED);
        initiative2.setStartDate(LocalDate.of(2022, 1, 10));
        initiative2.setServiceId("serviceId2");
        initiative2.setEnabled(Boolean.TRUE);

        InitiativeDTO initiative3 = new InitiativeDTO();
        initiative3.setInitiativeId("3c");
        initiative3.setInitiativeName("Iniziative name3");
        initiative3.setOrganizationName("Organization name3");
        initiative3.setStatus(InitiativeStatus.CLOSED);
        initiative3.setStartDate(LocalDate.of(2023, 1, 10));
        initiative3.setServiceId("serviceId3");
        initiative3.setEnabled(Boolean.TRUE);

        InitiativeDTO initiative4 = new InitiativeDTO();
        initiative4.setInitiativeId("4d");
        initiative4.setInitiativeName("Iniziative name4");
        initiative4.setOrganizationName("Organization name4");
        initiative4.setStatus(InitiativeStatus.PUBLISHED);
        initiative4.setStartDate(LocalDate.of(2023, 2, 10));
        initiative4.setServiceId("serviceId4");
        initiative4.setEnabled(Boolean.FALSE);

        InitiativeDTO initiative5 = new InitiativeDTO();
        initiative5.setInitiativeId("5e");
        initiative5.setInitiativeName("Iniziative name5");
        initiative5.setOrganizationName("Organization name5");
        initiative5.setStatus(InitiativeStatus.PUBLISHED);
        initiative5.setStartDate(LocalDate.of(2030, 1, 8));
        initiative5.setEndDate(LocalDate.of(2050, 3, 30));
        initiative5.setServiceId("serviceId5");
        initiative5.setEnabled(Boolean.TRUE);

        InitiativeDTO initiative6 = new InitiativeDTO();
        initiative6.setInitiativeId("6f");
        initiative6.setInitiativeName("Iniziative name6");
        initiative6.setOrganizationName("Organization name6");
        initiative6.setStatus(InitiativeStatus.PUBLISHED);
        initiative6.setStartDate(LocalDate.of(2020, 1, 8));
        initiative6.setEndDate(LocalDate.of(2023, 7, 30));
        initiative6.setServiceId("serviceId6");
        initiative6.setEnabled(Boolean.TRUE);

        InitiativeDTO initiative7 = new InitiativeDTO();
        initiative7.setInitiativeId("7g");
        initiative7.setInitiativeName("Iniziative name7");
        initiative7.setOrganizationName("Organization name7");
        initiative7.setStatus(InitiativeStatus.PUBLISHED);
        initiative7.setStartDate(LocalDate.now());
        initiative7.setEndDate(LocalDate.of(2030, 7, 30));
        initiative7.setServiceId("serviceId6");
        initiative7.setEnabled(Boolean.TRUE);


        InitiativeDTO initiative8 = new InitiativeDTO();
        initiative8.setInitiativeId("8h");
        initiative8.setInitiativeName("Iniziative name8");
        initiative8.setOrganizationName("Organization name8");
        initiative8.setStatus(InitiativeStatus.PUBLISHED);
        initiative8.setStartDate(LocalDate.of(2020, 1, 8));
        initiative8.setEndDate(LocalDate.now());
        initiative8.setServiceId("serviceId8");
        initiative8.setEnabled(Boolean.TRUE);

        List<InitiativeDTO> res = new ArrayList<>();
        res.add(initiative1);
        res.add(initiative2);
        res.add(initiative3);
        res.add(initiative4);
        res.add(initiative5);
        res.add(initiative6);
        res.add(initiative7);
        res.add(initiative8);


        return res;
    }
}
