package it.pagopa.swclient.mil.idpay.service;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import it.pagopa.swclient.mil.bean.Errors;
import it.pagopa.swclient.mil.idpay.ErrorCode;
import it.pagopa.swclient.mil.idpay.azurekeyvault.client.AzureKeyVaultClient;
import it.pagopa.swclient.mil.idpay.bean.cer.CertificateBundle;
import it.pagopa.swclient.mil.idpay.bean.secret.SecretBundle;
import it.pagopa.swclient.mil.idpay.client.AzureADRestClient;
import it.pagopa.swclient.mil.idpay.client.IdpayRestClient;
import it.pagopa.swclient.mil.idpay.client.bean.InitiativeDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Response;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

@ApplicationScoped
public class IdPayRestService {

    @RestClient
    AzureADRestClient azureADRestClient;

    @RestClient
    AzureKeyVaultClient azureKeyVaultClient;

    @ConfigProperty(name = "azure-cert.name")
    String certName;

    @Setter
    @RestClient
    IdpayRestClient idpayRestClient;

    public static final String VAULT = "https://vault.azure.net";

    @ConfigProperty(name = "azure-auth-api.identity")
    String identity;

    @ConfigProperty(name = "quarkus.rest-client.idpay-rest-api.url")
    String idpayUrl;

    private static final String BEARER = "Bearer ";

    @Startup
    void init() {
        Log.debugf("START: idpayRestClient %s ", idpayRestClient);
        idpayRestClient = null;
        Log.debugf("END: idpayRestClient generated: [%s] ", idpayRestClient);
    }

    public Uni<Void> checkOrGenerateClient() {
        if (this.idpayRestClient == null) {
            Log.debugf("IdPayRestService -> checkOrGenerateClient - client is null");

            return generateClient();
        } else {
            Log.debugf("IdPayRestService -> checkOrGenerateClient - client is not null , no action needed[%s]", this.idpayRestClient);

            return Uni.createFrom().voidItem();
        }
    }

    private Uni<Void> generateClient() {
        return getCertificate()
                .onItem()
                .transformToUni(Unchecked.function(certificateBundle -> {
                    if (checkCertIsValid(certificateBundle)) {
                        return getSecret().onFailure().transform(error -> {
                                    Log.errorf(error, "IdPayRestService -> checkOrGenerateClient: error while trying to getSecret");

                                    return new InternalServerErrorException(Response
                                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_SECRET_FOR_IDPAY), List.of(ErrorCode.ERROR_RETRIEVING_SECRET_FOR_IDPAY_MSG)))
                                            .build());
                                })
                                .chain(secretBundle -> createRestClient(certificateBundle.getCer(), secretBundle.getValue()));
                    } else {
                        return Uni.createFrom()
                                .failure(new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(List.of(ErrorCode.ERROR_CERTIFICATE_EXPIRED), List.of(ErrorCode.ERROR_CERTIFICATE_EXPIRED_MSG)))
                                        .build()));
                    }

                })).onFailure().invoke(Unchecked.consumer(failure -> {
                    Log.errorf(failure, "IdPayRestService -> checkOrGenerateClient: error while trying to getCertificate");

                    throw new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_CERT_FOR_IDPAY), List.of(ErrorCode.ERROR_RETRIEVING_CERT_FOR_IDPAY_MSG)))
                            .build());
                }));
    }

    private Uni<CertificateBundle> getCertificate() {
        Log.debugf("IdPayRestService -> getCertificate - certName: [%s]", certName);

        return azureADRestClient.getAccessToken(identity, VAULT)
                .onFailure().transform(t -> {
                    Log.errorf(t, "IdPayRestService -> getCertificate: Azure AD error response for certName [%s]", certName);

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_CALLING_AZUREAD_REST_SERVICES), List.of(ErrorCode.ERROR_CALLING_AZUREAD_REST_SERVICES_MSG)))
                            .build());
                }).chain(token -> {
                    Log.debugf("IdPayRestService -> getCertificate:  Azure AD service returned a 200 status, response: [%s]", token);

                    return azureKeyVaultClient.getCertificate(BEARER + token.getToken(), certName)
                            .onFailure().transform(t -> {
                                Log.errorf(t, "IdPayRestService -> getCertificate: Azure Key Vault error for certName [%s]", certName);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_CERT_FOR_IDPAY), List.of(ErrorCode.ERROR_RETRIEVING_CERT_FOR_IDPAY_MSG)))
                                        .build());
                            });
                });
    }

    private Uni<SecretBundle> getSecret() {
        Log.debugf("IdPayRestService -> getSecret - certName: [%s]", certName);

        return azureADRestClient.getAccessToken(identity, VAULT)
                .onFailure().transform(t -> {
                    Log.errorf(t, "IdPayRestService -> getSecret: Azure AD error response for certName [%s]", certName);

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_CALLING_AZUREAD_REST_SERVICES), List.of(ErrorCode.ERROR_CALLING_AZUREAD_REST_SERVICES_MSG)))
                            .build());
                }).chain(token -> {
                    Log.debugf("IdPayRestService -> getSecret:  Azure AD service returned a 200 status, response: [%s]", token);

                    return azureKeyVaultClient.getSecret(BEARER + token.getToken(), certName)
                            .onFailure().transform(t -> {
                                Log.errorf(t, "IdPayRestService -> getSecret: Azure Key Vault error for certName [%s]", certName);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_SECRET_FOR_IDPAY), List.of(ErrorCode.ERROR_RETRIEVING_SECRET_FOR_IDPAY_MSG)))
                                        .build());
                            });
                });
    }

    private Uni<Void> createRestClient(String certificate, String pkcs) {
        Log.debugf("IdPayRestService -> createRestClient: Generating private key with certificate and pkcs: [%s], [%s]", certificate, pkcs);

        try (InputStream p12Stream = new ByteArrayInputStream(Base64.getDecoder().decode(pkcs));
             InputStream cerStream = new ByteArrayInputStream(Base64.getDecoder().decode(certificate))) {
            KeyStore ephemeralKeyStore = KeyStore.getInstance("JKS");
            ephemeralKeyStore.load(null);

            /*
             * Load certificate.
             */
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(cerStream);
            ephemeralKeyStore.setCertificateEntry("certificate", cert);
            char[] nopwd = new char[0];

            /*
             * Load private key.
             */
            KeyStore p12 = KeyStore.getInstance("PKCS12");
            p12.load(p12Stream, nopwd);
            Iterator<String> aliases = p12.aliases().asIterator();
            PrivateKey privateKey = null;
            while (aliases.hasNext() && privateKey == null) {
                String alias = aliases.next();
                if (p12.isKeyEntry(alias)) {
                    privateKey = (PrivateKey) p12.getKey(alias, nopwd);
                    ephemeralKeyStore.setKeyEntry("private-key", privateKey, nopwd, new Certificate[]{
                            cert
                    });
                }
            }

            URL url = new URL(idpayUrl);
            Log.debugf("IdPayRestService -> createRestClient: idpay url: [%s]", url);

            /*
             * Build REST client.
             */
            idpayRestClient = RestClientBuilder.newBuilder()
                    .baseUrl(url)
                    .keyStore(ephemeralKeyStore, new String(nopwd))
                    .build(IdpayRestClient.class);

            Log.debugf("IdPayRestService -> createRestClient: rest client generated correctly: [%s]", idpayRestClient);
            return Uni.createFrom().voidItem();

        } catch (IOException | KeyStoreException | UnrecoverableKeyException | CertificateException |
                 NoSuchAlgorithmException e) {
            Log.errorf(e, "IdPayRestService -> createRestClient: error generating key store with certificate and pkcs: [%s], [%s]", certificate, pkcs);
            idpayRestClient = null;

            throw new InternalServerErrorException(Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Errors(List.of(ErrorCode.ERROR_GENERATING_KEY_STORE), List.of(ErrorCode.ERROR_GENERATING_KEY_STORE_MSG)))
                    .build());
        }
    }

    private boolean checkCertIsValid(CertificateBundle cert) {
        long now = Instant.now().getEpochSecond();

        return cert.getAttributes().getEnabled() && (cert.getAttributes().getNbf() <= now && cert.getAttributes().getExp() > now);
    }

    public Uni<List<InitiativeDTO>> getMerchantInitiativeList(String idpayMerchantId, String xAcquirerId) {
        return idpayRestClient.getMerchantInitiativeList(idpayMerchantId, xAcquirerId);
    }

}
