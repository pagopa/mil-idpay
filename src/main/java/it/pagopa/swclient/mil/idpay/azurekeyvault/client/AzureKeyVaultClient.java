/*
 * AzureKeyVaultClient.java
 *
 * 23 lug 2023
 */
package it.pagopa.swclient.mil.idpay.azurekeyvault.client;

import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.*;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.rest.client.reactive.ClientQueryParam;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 *
 * @author Antonio Tarricone
 */
@RegisterRestClient(configKey = "azure-key-vault-api")
public interface AzureKeyVaultClient {
    /**
     *
     * @param authorization
     * @param keyName
     * @param createKeyRequest
     * @return
     */
    @Path("/keys/{keyName}/create")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientQueryParam(name = "api-version", value = "${azure-key-vault-api.version}")
    Uni<DetailedKey> createKey(
            @HeaderParam("Authorization") String authorization,
            @PathParam("keyName") String keyName,
            CreateKeyRequest createKeyRequest);



    /**
     *
     * @param authorization
     * @param keyName
     * @return
     */
    @Path("/keys/{keyName}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ClientQueryParam(name = "api-version", value = "${azure-key-vault-api.version}")
    Uni<DetailedKey> getKey(
            @HeaderParam("Authorization") String authorization,
            @PathParam("keyName") String keyName);


    /**
     *
     * @param authorization
     * @param keyName
     * @param keyVersion
     * @param unwrapKeyRequest
     * @return
     */
    @Path("/keys/{keyName}/{keyVersion}/unwrapkey")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientQueryParam(name = "api-version", value = "${azure-key-vault-api.version}")
    Uni<SignResponse> unwrapKey(
            @HeaderParam("Authorization") String authorization,
            @PathParam("keyName") String keyName,
            @PathParam("keyVersion") String keyVersion,
            UnwrapKeyRequest unwrapKeyRequest);
}
