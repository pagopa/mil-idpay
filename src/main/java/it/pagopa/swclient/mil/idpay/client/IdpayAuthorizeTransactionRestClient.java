package it.pagopa.swclient.mil.idpay.client;

import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.bean.AuthTransactionResponse;
import it.pagopa.swclient.mil.idpay.bean.AuthorizeTransaction;
import it.pagopa.swclient.mil.idpay.bean.PublicKeyIDPay;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "idpay-rest-api")
public interface IdpayAuthorizeTransactionRestClient {

    @GET
    @Path("/zonePublicKey")
    @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${idpay-rest-client.apim-subscription-key}", required = false)
    Uni<PublicKeyIDPay> retrieveIdpayPublicKey();

    @POST
    @Path("idpay/mil/payment/cie/{idpayTransactionId}/authorize")
    @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${idpay-rest-client.apim-subscription-key}", required = false)
    Uni<AuthTransactionResponse> authorize(@HeaderParam("x-merchant-id") @NotNull String xMerchantId, @HeaderParam("x-acquirer-id") String xAcquirerId, AuthorizeTransaction authorizeTransaction);
}
