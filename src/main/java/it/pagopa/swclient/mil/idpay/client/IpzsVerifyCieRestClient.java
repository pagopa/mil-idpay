package it.pagopa.swclient.mil.idpay.client;

import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.IpzsVerifyCieRequest;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.IpzsVerifyCieResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;

@RegisterRestClient(configKey = "ipzs-rest-api")
public interface IpzsVerifyCieRestClient {

    @POST
    @Path("/api/identitycards")
    Uni<IpzsVerifyCieResponse> identitycards(@RestQuery @NotNull String transactionId, IpzsVerifyCieRequest ipzsVerifyCieRequest);
}
