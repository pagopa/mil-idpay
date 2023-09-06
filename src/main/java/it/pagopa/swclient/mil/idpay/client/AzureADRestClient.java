package it.pagopa.swclient.mil.idpay.client;

import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.client.bean.azure.AccessToken;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.IpzsVerifyCieRequest;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.IpzsVerifyCieResponse;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "azuread-rest-api")
public interface AzureADRestClient {

    @POST
    @Path("/{tenantId}/oauth2/v2.0/token")
    Uni<AccessToken> token(@PathParam("tenantId") String tenantId, String request);
}
