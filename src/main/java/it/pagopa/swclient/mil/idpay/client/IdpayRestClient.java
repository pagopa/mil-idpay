package it.pagopa.swclient.mil.idpay.client;

import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.bean.AuthTransactionResponse;
import it.pagopa.swclient.mil.idpay.bean.PinBlockDTO;
import it.pagopa.swclient.mil.idpay.bean.PublicKeyIDPay;
import it.pagopa.swclient.mil.idpay.client.bean.*;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@RegisterRestClient(configKey = "idpay-rest-api")
public interface IdpayRestClient {

    /*
     * INITIATIVES
     */
    @GET
    @Path("/idpay/mil/merchant/initiatives")
    @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${idpay-rest-client.apim-subscription-key}", required = false)
    Uni<List<InitiativeDTO>> getMerchantInitiativeList(
            @HeaderParam("x-merchant-fiscalcode") @NotNull String idpayMerchantId,
            @HeaderParam("x-acquirer-id") @NotNull String xAcquirerId);

    /*
     * TRANSACTIONS
     */
    @POST
    @Path("/idpay/mil/payment")
    @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${idpay-rest-client.apim-subscription-key}", required = false)
    Uni<TransactionResponse> createTransaction(
            @HeaderParam("x-merchant-fiscalcode") @NotNull String idpayMerchantId,
            @HeaderParam("x-acquirer-id") @NotNull String xAcquirerId,
            TransactionCreationRequest transactionCreationRequest);

    @GET
    @Path("/idpay/mil/payment/{transactionId}/status")
    @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${idpay-rest-client.apim-subscription-key}", required = false)
    Uni<SyncTrxStatus> getStatusTransaction(
            @HeaderParam("x-merchant-fiscalcode") @NotNull String idpayMerchantId,
            @HeaderParam("x-acquirer-id") @NotNull String xAcquirerId,
            @PathParam("transactionId") String transactionId);

    @PUT
    @Path("/idpay/mil/payment/idpay-code/{transactionId}/preview")
    @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${idpay-rest-client.apim-subscription-key}", required = false)
    Uni<PreAuthPaymentResponseDTO> putPreviewPreAuthPayment(
            @HeaderParam("x-merchant-fiscalcode") @NotNull String idpayMerchantId,
            @HeaderParam("x-acquirer-id") @NotNull String xAcquirerId,
            @PathParam("transactionId") String transactionId);

    @DELETE
    @Path("/idpay/mil/payment/{transactionId}")
    @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${idpay-rest-client.apim-subscription-key}", required = false)
    Uni<Void> deleteTransaction(
            @HeaderParam("x-merchant-fiscalcode") @NotNull String idpayMerchantId,
            @HeaderParam("x-acquirer-id") @NotNull String xAcquirerId,
            @PathParam("transactionId") String transactionId);

    /*
     * AUTHORIZE TRANSACTIONS
     */
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
