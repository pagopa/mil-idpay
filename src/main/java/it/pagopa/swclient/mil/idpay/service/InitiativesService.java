package it.pagopa.swclient.mil.idpay.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.bean.Errors;
import it.pagopa.swclient.mil.idpay.ErrorCode;
import it.pagopa.swclient.mil.idpay.bean.Initiative;
import it.pagopa.swclient.mil.idpay.bean.Initiatives;
import it.pagopa.swclient.mil.idpay.client.IdpayInitiativesRestClient;
import it.pagopa.swclient.mil.idpay.client.bean.InitiativeStatus;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class InitiativesService {

    @RestClient
    IdpayInitiativesRestClient idpayInitiativesRestClient;

    public Uni<?> getInitiatives(CommonHeader headers) {

        Log.debugf("InitiativesService -> getInitiatives - Input parameters: %s", headers);

        return idpayInitiativesRestClient.getMerchantInitiativeList(headers.getMerchantId())
                .onItemOrFailure().transformToUni((res, error) -> {
                    if (error != null) {         //Errore da IdPay
                        Log.errorf(error, "InitiativesService -> getInitiatives: error in calling idpay rest services");
                        if (error instanceof ClientWebApplicationException webEx && webEx.getResponse().getStatus() == 404) {
                            Log.errorf(error, " InitiativesService -> getInitiatives: idpay NOT FOUND for MerchantId [%s]", headers.getMerchantId());
                            Errors errors = new Errors(List.of(ErrorCode.ERROR_NOT_FOUND_IDPAY_REST_SERVICES), List.of(ErrorCode.ERROR_NOT_FOUND_IDPAY_REST_SERVICES_MSG));
                            return Uni.createFrom().item(errors);
                        } else {
                            Log.errorf(error, "InitiativesService -> getInitiatives: idpay error response for MerchantId [%s]", headers.getMerchantId());
                            Errors errors = new Errors(List.of(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES), List.of(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES_MSG));
                            return Uni.createFrom().item(errors);
                        }
                    } else {
                            Log.debugf("InitiativesService -> getInitiatives: idpay getMerchantInitiativeList service returned a 200 status, response: [%s]", res);

                            LocalDate today = LocalDate.now();

                            List<Initiative> iniList = res.stream().filter(ini -> {
                                return (InitiativeStatus.PUBLISHED == ini.getStatus()  && Boolean.TRUE.equals(ini.getEnabled())
                                        && (today.isAfter(ini.getStartDate()) || today.isEqual(ini.getStartDate()))
                                        && (ini.getEndDate() == null || (today.isBefore(ini.getEndDate()) || today.isEqual(ini.getEndDate()))));
                            })
                                    .map(fIni -> new Initiative(fIni.getInitiativeId(), fIni.getInitiativeName(), fIni.getOrganizationName())).toList();

                            Initiatives inis = new Initiatives();
                            inis.setInitiatives(iniList);

                            return Uni.createFrom().item(inis);

                        }
                     });
    }
}
