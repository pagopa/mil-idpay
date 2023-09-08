/*
 * AzureKeyVaultClient.java
 *
 * 23 lug 2023
 */
package it.pagopa.swclient.mil.idpay.azurekeyvault.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.rest.client.reactive.ClientQueryParam;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.CreateKeyRequest;
import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.CreateKeyResponse;
import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.GetKeyResponse;
import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.GetKeyVersionsResponse;
import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.GetKeysResponse;
import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.SignRequest;
import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.SignResponse;
import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.VerifySignatureRequest;
import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.VerifySignatureResponse;
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
    Uni<CreateKeyResponse> createKey(
            @HeaderParam("Authorization") String authorization,
            @PathParam("keyName") String keyName,
            CreateKeyRequest createKeyRequest);

    /**
     *
     * @param authorization
     * @return
     */
    @Path("/keys")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ClientQueryParam(name = "api-version", value = "${azure-key-vault-api.version}")
    Uni<GetKeysResponse> getKeys(
            @HeaderParam("Authorization") String authorization);

    /**
     *
     * @param authorization
     * @param keyName
     * @param keyVersion
     * @return
     */
    @Path("/keys/{keyName}/{keyVersion}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ClientQueryParam(name = "api-version", value = "${azure-key-vault-api.version}")
    Uni<GetKeyResponse> getKey(
            @HeaderParam("Authorization") String authorization,
            @PathParam("keyName") String keyName,
            @PathParam("keyVersion") String keyVersion);


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
    Uni<GetKeyResponse> getKey(
            @HeaderParam("Authorization") String authorization,
            @PathParam("keyName") String keyName);

    /**
     *
     * @param authorization
     * @param keyName
     * @return
     */
    @Path("/keys/{keyName}/versions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ClientQueryParam(name = "api-version", value = "${azure-key-vault-api.version}")
    Uni<GetKeyVersionsResponse> getKeyVersions(
            @HeaderParam("Authorization") String authorization,
            @PathParam("keyName") String keyName);

    /**
     *
     * @param authorization
     * @param keyName
     * @param keyVersion
     * @param signRequest
     * @return
     */
    @Path("/keys/{keyName}/{keyVersion}/sign")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientQueryParam(name = "api-version", value = "${azure-key-vault-api.version}")
    Uni<SignResponse> sign(
            @HeaderParam("Authorization") String authorization,
            @PathParam("keyName") String keyName,
            @PathParam("keyVersion") String keyVersion,
            SignRequest signRequest);

    /**
     *
     * @param authorization
     * @param keyName
     * @param keyVersion
     * @param verifySignatureRequest
     * @return
     */
    @Path("/keys/{keyName}/{keyVersion}/verify")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientQueryParam(name = "api-version", value = "${azure-key-vault-api.version}")
    Uni<VerifySignatureResponse> verifySignature(
            @HeaderParam("Authorization") String authorization,
            @PathParam("keyName") String keyName,
            @PathParam("keyVersion") String keyVersion,
            VerifySignatureRequest verifySignatureRequest);
}
