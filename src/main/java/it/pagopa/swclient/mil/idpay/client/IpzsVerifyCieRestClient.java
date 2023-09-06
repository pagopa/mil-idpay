package it.pagopa.swclient.mil.idpay.client;

import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.IpzsVerifyCieRequest;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.IpzsVerifyCieResponse;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "ipzs-rest-api")
public interface IpzsVerifyCieRestClient {

    @POST
    @Path("/api/identitycards?transactionID={transactionId}")
    Uni<IpzsVerifyCieResponse> identitycards(@PathParam("transactionId") String transactionId, IpzsVerifyCieRequest ipzsVerifyCieRequest);
}
