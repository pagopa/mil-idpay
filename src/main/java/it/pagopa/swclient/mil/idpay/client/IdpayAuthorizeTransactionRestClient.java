package it.pagopa.swclient.mil.idpay.client;

import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.bean.AuthTransactionResponse;
import it.pagopa.swclient.mil.idpay.bean.PinBlockDTO;
import it.pagopa.swclient.mil.idpay.bean.PublicKeyIDPay;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "idpay-rest-api")
public interface IdpayAuthorizeTransactionRestClient {

    @GET
    @Path("/idpay/mil/payment/publickey")
    @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${idpay-rest-client.apim-subscription-key}", required = false)
    Uni<PublicKeyIDPay> retrieveIdpayPublicKey(
            @HeaderParam("x-acquirer-id") String xAcquirerId);

    @PUT
    @Path("/idpay/mil/payment/idpay-code/{idpayTransactionId}/authorize")
    @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${idpay-rest-client.apim-subscription-key}", required = false)
    Uni<AuthTransactionResponse> authorize(
            @HeaderParam("x-merchant-fiscalcode") @NotNull String xMerchantFiscalCode,
            @HeaderParam("x-acquirer-id") String xAcquirerId,
            @PathParam("idpayTransactionId") String idpayTransactionId,
            PinBlockDTO pinBlockDTO);
}
