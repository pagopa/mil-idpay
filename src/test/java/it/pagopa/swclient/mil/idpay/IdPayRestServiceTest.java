package it.pagopa.swclient.mil.idpay;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.azurekeyvault.client.AzureKeyVaultClient;
import it.pagopa.swclient.mil.idpay.bean.AuthTransactionResponse;
import it.pagopa.swclient.mil.idpay.bean.PinBlockDTO;
import it.pagopa.swclient.mil.idpay.bean.PublicKeyIDPay;
import it.pagopa.swclient.mil.idpay.bean.cer.Attributes;
import it.pagopa.swclient.mil.idpay.bean.cer.CertificateBundle;
import it.pagopa.swclient.mil.idpay.bean.secret.SecretBundle;
import it.pagopa.swclient.mil.idpay.client.AzureADRestClient;
import it.pagopa.swclient.mil.idpay.client.IdpayRestClient;
import it.pagopa.swclient.mil.idpay.client.bean.*;
import it.pagopa.swclient.mil.idpay.client.bean.azure.AccessToken;
import it.pagopa.swclient.mil.idpay.resource.InitiativesResource;
import it.pagopa.swclient.mil.idpay.service.IdPayRestService;
import it.pagopa.swclient.mil.idpay.util.TransactionsTestData;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(InitiativesResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IdPayRestServiceTest {

    @InjectMock
    @RestClient
    AzureADRestClient azureADRestClient;

    @InjectMock
    @RestClient
    AzureKeyVaultClient azureKeyVaultClient;

    @InjectMock
    @RestClient
    IdpayRestClient idpayRestClient;

    @Inject
    IdPayRestService idPayRestService;

    Map<String, String> validMilHeaders;

    AccessToken azureAdAccessToken;

    CertificateBundle certificateBundle;

    CertificateBundle certificateExpiredBundle;

    SecretBundle secretBundle;

    String merchantId;

    String acquirerId;

    String transactionId;

    @BeforeAll
    void createTestObjects() {
        validMilHeaders = TransactionsTestData.getMilHeaders();
        merchantId = validMilHeaders.get("MerchantId");
        acquirerId = validMilHeaders.get("AcquirerId");
        transactionId = "123456";
        azureAdAccessToken = TransactionsTestData.getAzureADAccessToken();
        certificateBundle = TransactionsTestData.getCertificateBundle();
        certificateExpiredBundle = TransactionsTestData.getExpiredCertificateBundle();
        secretBundle = TransactionsTestData.getSecretBundle();
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void getCertificateTest_KOCertTok500() {

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));

        Response response = given()
                .headers(validMilHeaders)
                .when()
                .get()
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CALLING_AZUREAD_REST_SERVICES));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void getCertificateTest_KOCert500() {

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateExpiredBundle));

        Response response = given()
                .headers(validMilHeaders)
                .when()
                .get()
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CERTIFICATE_EXPIRED));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void getCertificateTest_KOCertKV500() {

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));

        Response response = given()
                .headers(validMilHeaders)
                .when()
                .get()
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_CERT_FOR_IDPAY));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void getSecretTest_KOSecr500() {

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));

        Response response = given()
                .headers(validMilHeaders)
                .when()
                .get()
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_SECRET_FOR_IDPAY));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void getSecretTest_KOSecrTok500() {
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken))
                .thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Response response = given()
                .headers(validMilHeaders)
                .when()
                .get()
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertNull(response.jsonPath().getList("initiatives"));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void createClientTest_OK() {

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        idPayRestService.createRestClient(certificateBundle.getCer(), secretBundle.getValue());

        Assertions.assertNotNull(idPayRestService.getIdpayRestClient());

        idPayRestService.setIdpayRestClient(null);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void createClientTest_OKNotNull() {

        idPayRestService.setIdpayRestClient(idpayRestClient);

        idPayRestService.checkOrGenerateClient();

        Assertions.assertNotNull(idPayRestService.getIdpayRestClient());

        idPayRestService.setIdpayRestClient(null);

    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void createClientTest_KO500() {

        InternalServerErrorException thrown = Assertions.assertThrows(InternalServerErrorException.class, () ->
                idPayRestService.createRestClient("cert", "pkcs"));

        Assertions.assertEquals(500, thrown.getResponse().getStatus());
    }

    @Test
    void getMerchantTest_OK() {

        idPayRestService.setIdpayRestClient(idpayRestClient);

        Uni<List<InitiativeDTO>> result = idPayRestService.getMerchantInitiativeList(merchantId, acquirerId);

        Assertions.assertNotNull(result);

        idPayRestService.setIdpayRestClient(null);
    }

    @Test
    void createTransactionTest_OK() {

        idPayRestService.setIdpayRestClient(idpayRestClient);

        TransactionCreationRequest transactionCreationRequest = new TransactionCreationRequest();

        Uni<TransactionResponse> result = idPayRestService.createTransaction(merchantId, acquirerId, transactionCreationRequest);

        Assertions.assertNotNull(result);

        idPayRestService.setIdpayRestClient(null);
    }

    @Test
    void deleteTransactionTest_OK() {

        idPayRestService.setIdpayRestClient(idpayRestClient);

        Uni<Void> result = idPayRestService.deleteTransaction(merchantId, acquirerId, transactionId);

        Assertions.assertNotNull(result);

        idPayRestService.setIdpayRestClient(null);
    }

    @Test
    void retrieveIdpayeyTest_OK() {

        idPayRestService.setIdpayRestClient(idpayRestClient);

        Uni<PublicKeyIDPay> result = idPayRestService.retrieveIdpayPublicKey(acquirerId);

        Assertions.assertNotNull(result);

        idPayRestService.setIdpayRestClient(null);
    }

    @Test
    void authorizeTest_OK() {

        idPayRestService.setIdpayRestClient(idpayRestClient);

        PinBlockDTO pinBlockDTO = new PinBlockDTO();

        Uni<AuthTransactionResponse> result = idPayRestService.authorize(merchantId, acquirerId, transactionId, pinBlockDTO);

        Assertions.assertNotNull(result);

        idPayRestService.setIdpayRestClient(null);
    }

    @Test
    void putPreviewPreAuthPaymentTest_OK() {

        idPayRestService.setIdpayRestClient(idpayRestClient);

        Uni<PreAuthPaymentResponseDTO> result = idPayRestService.putPreviewPreAuthPayment(merchantId, acquirerId, transactionId);

        Assertions.assertNotNull(result);

        idPayRestService.setIdpayRestClient(null);
    }

    @Test
    void getStatusTransactionTest_OK() {

        idPayRestService.setIdpayRestClient(idpayRestClient);

        Uni<SyncTrxStatus> result = idPayRestService.getStatusTransaction(merchantId, acquirerId, transactionId);

        Assertions.assertNotNull(result);

        idPayRestService.setIdpayRestClient(null);
    }

    @Test
    void checkCertIsValidTest_TFT() {
        Attributes attrs = new Attributes();
        attrs.setEnabled(true);
        attrs.setNbf(Instant.now().getEpochSecond() + 100);
        attrs.setExp(Instant.now().getEpochSecond() + 100);

        CertificateBundle cert = CertificateBundle
                .builder()
                .attributes(attrs)
                .build();

        boolean result = idPayRestService.checkCertIsValid(cert);

        Assertions.assertFalse(result);
    }

    @Test
    void checkCertIsValidTest_FTT() {
        Attributes attrs = new Attributes();
        attrs.setEnabled(false);
        attrs.setNbf(Instant.now().getEpochSecond() - 100);
        attrs.setExp(Instant.now().getEpochSecond() + 100);

        CertificateBundle cert = CertificateBundle
                .builder()
                .attributes(attrs)
                .build();

        boolean result = idPayRestService.checkCertIsValid(cert);

        Assertions.assertFalse(result);
    }
}
