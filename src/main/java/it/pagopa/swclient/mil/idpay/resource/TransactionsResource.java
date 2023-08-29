package it.pagopa.swclient.mil.idpay.resource;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.idpay.ErrorCode;
import it.pagopa.swclient.mil.idpay.IdpayConstants;
import it.pagopa.swclient.mil.idpay.bean.CreateTransaction;
import it.pagopa.swclient.mil.idpay.service.TransactionsService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;

@Path("/transactions")
public class TransactionsResource {
    @Inject
    TransactionsService transactionsService;

    /**
     * The value of the Max-Retries header to be sent in response to the createTransaction API
     */
    @ConfigProperty(name="idpay.transactiont.max-retry", defaultValue = "3")
    int idpayTransactionMaxRetry;

    /**
     * The value of the Retry-After header to be sent in response to the createTransaction API
     */
    @ConfigProperty(name="idpay.transaction.retry-after", defaultValue = "30")
    int idpayTransactionRetryAfter;

    /**
     * The base URL for the location header returned by the createTransaction (i.e. the API management base URL)
     */
    @ConfigProperty(name="idpay.transaction.location.base-url")
    String idpayTransactionLocationBaseURL;


    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"NoticePayer", "SlavePos"})
    public Uni<Response> createTransaction(
            @Valid @BeanParam CommonHeader headers,
            @Valid
            @NotNull(message = "[" + ErrorCode.CREATE_TRANSACTION_MUST_NOT_BE_EMPTY + "] request must not be empty")
            CreateTransaction createTransaction) {

        Log.debugf("TransactionsResource -> createTransaction - Input createTransaction: %s, %s", headers, createTransaction);

        return transactionsService.createTransaction(headers, createTransaction).chain(res -> {
            Log.debugf("TransactionsResource -> TransactionsService -> createTransaction - Response %s", res);

            Response.ResponseBuilder responseBuilder = Response.status(Status.CREATED);
            responseBuilder
                    .location(getTransactionURI(res.getMilTransactionId()))
                    .header("Retry-After", idpayTransactionRetryAfter)
                    .header("Max-Retries", idpayTransactionMaxRetry);

            return Uni.createFrom().item(responseBuilder.entity(res).build());
        });
    }

    @GET
    @Path("/{transactionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"NoticePayer", "SlavePos"})
    public Uni<Response> getTransaction(@Valid @BeanParam CommonHeader headers,
                                          @Pattern(regexp = IdpayConstants.TRANSACTION_ID_REGEX,
                                                  message = "[" + ErrorCode.ERROR_TRANSACTION_ID_MUST_MATCH_REGEXP + "] transactionId must match \"{regexp}\"")
                                          @PathParam(value = "transactionId") String transactionId) {

        Log.debugf("TransactionsResource -> getTransaction - Input parameters: %s, transactionId: %s", headers, transactionId);

        return transactionsService.getTransaction(headers, transactionId).chain(res -> {
            Log.debugf("TransactionsResource -> TransactionsService -> getTransaction - Response %s", res);

            return Uni.createFrom().item(
                    Response.status(Response.Status.OK)
                            .entity(res)
                            .build());
        });
    }

    @DELETE
    @Path("/{transactionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"NoticePayer", "SlavePos"})
    public Uni<Response> cancelTransaction(@Valid @BeanParam CommonHeader headers,
                                        @Pattern(regexp = IdpayConstants.TRANSACTION_ID_REGEX,
                                                message = "[" + ErrorCode.ERROR_TRANSACTION_ID_MUST_MATCH_REGEXP + "] transactionId must match \"{regexp}\"")
                                        @PathParam(value = "transactionId") String transactionId) {

        Log.debugf("TransactionsResource -> cancelTransaction - Input parameters: %s, transactionId: %s", headers, transactionId);

        return transactionsService.cancelTransaction(headers, transactionId).chain(() -> {

            return Uni.createFrom().item(
                    Response.status(Status.NO_CONTENT).build());
        });
    }

    private URI getTransactionURI(String milTransactionId) {
        return URI.create(idpayTransactionLocationBaseURL + "/transactions/" + milTransactionId);
    }


}
