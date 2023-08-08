package it.pagopa.swclient.mil.idpay.resource;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.bean.Errors;
import it.pagopa.swclient.mil.idpay.ErrorCode;
import it.pagopa.swclient.mil.idpay.bean.CreateTransaction;
import it.pagopa.swclient.mil.idpay.bean.Transaction;
import it.pagopa.swclient.mil.idpay.service.TransactionsService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

        return transactionsService.createTransaction(headers, createTransaction).onItem().transformToUni(res -> {
                        if (res instanceof Errors) {            //Errore gestito dal service
                            Log.errorf("TransactionsResource -> TransactionsService -> createTransaction error [%s]", ((Errors) res));
                            return Uni.createFrom().item(
                                    Response.status(Status.INTERNAL_SERVER_ERROR)
                                            .entity(res)
                                            .build());
                        }
                        else {       //Risposta positiva dal service
                            Log.debugf("TransactionsResource -> TransactionsService -> createTransaction - Response %s", res);

                            Response.ResponseBuilder responseBuilder = Response.status(Status.CREATED);
                            responseBuilder
                                    .location(getTransactionURI(((Transaction) res).getMilTransactionId()))
                                    .header("Retry-After", idpayTransactionRetryAfter)
                                    .header("Max-Retries", idpayTransactionMaxRetry);

                            return Uni.createFrom().item(responseBuilder.entity(res).build());
                        }

                    });
    }

    private URI getTransactionURI(String milTransactionId) {
        return URI.create(idpayTransactionLocationBaseURL + "/payments/" + milTransactionId);
    }


}
