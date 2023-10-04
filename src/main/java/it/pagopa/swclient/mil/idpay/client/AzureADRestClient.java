package it.pagopa.swclient.mil.idpay.client;

import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.client.bean.azure.AccessToken;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.IpzsVerifyCieRequest;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.IpzsVerifyCieResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "azuread-rest-api")
public interface AzureADRestClient {

    /**
     *
     * @param tenantId
     * @param grantType
     * @param clientId
     * @param clientSecret
     * @param scope
     * @return
     */
    @Path("/{tenantId}/oauth2/v2.0/token")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<AccessToken> getAccessToken(
            @PathParam("tenantId") String tenantId,
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("scope") String scope);
}
