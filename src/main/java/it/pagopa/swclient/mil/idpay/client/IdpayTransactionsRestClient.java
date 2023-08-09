package it.pagopa.swclient.mil.idpay.client;

import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionCreationRequest;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "idpay-rest-api")
public interface IdpayTransactionsRestClient {

    @POST
    @Path("")
    @ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${idpay-rest-client.apim-subscription-key}", required = false)
    Uni<TransactionResponse> createTransaction(@HeaderParam("x-merchant-fiscalcode") @NotNull String xMerchantFiscalcode, @HeaderParam("x-acquirer-id") @NotNull String xAcquirerId, TransactionCreationRequest transactionCreationRequest);
}
