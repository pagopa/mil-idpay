package it.pagopa.swclient.mil.idpay.resource;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.idpay.service.InitiativesService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/initiatives")
public class InitiativesResource {

    @Inject
    InitiativesService initiativesService;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"PayWithIDPay"})
    public Uni<Response> getInitiatives(@Valid @BeanParam CommonHeader headers) {

        Log.debugf("InitiativesResource -> getInitiatives - Input parameters: %s", headers);

        return initiativesService.getInitiatives(headers).chain(res -> {
                    Log.debugf("InitiativesResource -> InitiativesService -> getInitiatives - Response %s", res);

                    return Uni.createFrom().item(
                            Response.status(Response.Status.OK)
                                    .entity(res)
                                    .build());
                });
    }
}
