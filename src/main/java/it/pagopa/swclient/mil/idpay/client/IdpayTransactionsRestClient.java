package it.pagopa.swclient.mil.idpay.client;

import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.client.bean.PreAuthPaymentResponseDTO;
import it.pagopa.swclient.mil.idpay.client.bean.SyncTrxStatus;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionCreationRequest;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "idpay-rest-api")
public interface IdpayTransactionsRestClient {

    @POST
    @Path("/idpay/mil/payment")
    @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${idpay-rest-client.apim-subscription-key}", required = false)
    Uni<TransactionResponse> createTransaction(@HeaderParam("x-merchant-fiscalcode") @NotNull String idpayMerchantId, @HeaderParam("x-acquirer-id") @NotNull String xAcquirerId, TransactionCreationRequest transactionCreationRequest);

    @GET
    @Path("/idpay/mil/payment/{transactionId}/status")
    @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${idpay-rest-client.apim-subscription-key}", required = false)
    Uni<SyncTrxStatus> getStatusTransaction(@HeaderParam("x-merchant-fiscalcode") @NotNull String idpayMerchantId, @HeaderParam("x-acquirer-id") @NotNull String xAcquirerId, @PathParam("transactionId") String transactionId);

    @PUT
    @Path("/idpay/mil/payment/idpay-code/{transactionId}/preview")
    @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${idpay-rest-client.apim-subscription-key}", required = false)
    Uni<PreAuthPaymentResponseDTO> putPreviewPreAuthPayment(@HeaderParam("x-merchant-fiscalcode") @NotNull String idpayMerchantId, @HeaderParam("x-acquirer-id") @NotNull String xAcquirerId, @PathParam("transactionId") String transactionId);

    @DELETE
    @Path("/idpay/mil/payment/{transactionId}")
    @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${idpay-rest-client.apim-subscription-key}", required = false)
    Uni<Void> deleteTransaction(@HeaderParam("x-merchant-fiscalcode") @NotNull String idpayMerchantId, @HeaderParam("x-acquirer-id") @NotNull String xAcquirerId, @PathParam("transactionId") String transactionId);
}
