package it.pagopa.swclient.mil.idpay;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.azurekeyvault.client.AzureKeyVaultClient;
import it.pagopa.swclient.mil.idpay.bean.cer.CertificateBundle;
import it.pagopa.swclient.mil.idpay.bean.secret.SecretBundle;
import it.pagopa.swclient.mil.idpay.client.AzureADRestClient;
import it.pagopa.swclient.mil.idpay.client.IdpayRestClient;
import it.pagopa.swclient.mil.idpay.client.bean.InitiativeDTO;
import it.pagopa.swclient.mil.idpay.client.bean.azure.AccessToken;
import it.pagopa.swclient.mil.idpay.resource.InitiativesResource;
import it.pagopa.swclient.mil.idpay.util.InitiativesTestData;
import it.pagopa.swclient.mil.idpay.util.TransactionsTestData;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(InitiativesResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InitiativesResourceTest {

    @InjectMock
    @RestClient
    IdpayRestClient idpayRestClient;

    @InjectMock
    @RestClient
    AzureADRestClient azureADRestClient;

    @InjectMock
    @RestClient
    AzureKeyVaultClient azureKeyVaultClient;

    Map<String, String> validMilHeaders;

    List<InitiativeDTO> initiativesList;

    AccessToken azureAdAccessToken;

    CertificateBundle certificateBundle;

    SecretBundle secretBundle;

    @BeforeAll
    void createTestObjects() {
        validMilHeaders = InitiativesTestData.getMilHeaders();
        initiativesList = InitiativesTestData.getMerchantInitiativeList();
        azureAdAccessToken = TransactionsTestData.getAzureADAccessToken();
        certificateBundle = TransactionsTestData.getCertificateBundle();
        secretBundle = TransactionsTestData.getSecretBundle();

    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void getMerchantInitiativeListTest_OK() {

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Mockito.when(idpayRestClient.getMerchantInitiativeList(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(initiativesList));

        Response response = given()
                .headers(validMilHeaders)
                .when()
                .get()
                .then()
                .extract()
                .response();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(6, response.jsonPath().getList("initiatives").size());
        Assertions.assertNull(response.jsonPath().getList("errors"));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void getMerchantInitiativeListTest_KO404() {

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Mockito.when(idpayRestClient.getMerchantInitiativeList(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(404)));

        Response response = given()
                .headers(validMilHeaders)
                .when()
                .get()
                .then()
                .extract()
                .response();

        Assertions.assertEquals(404, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());
        Assertions.assertNull(response.jsonPath().getList("initiatives"));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void getMerchantInitiativeListTest_KO500() {

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Mockito.when(idpayRestClient.getMerchantInitiativeList(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(500)));

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
    void getMerchantInitiativeListTest_KO500RuntimeEx() {

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Mockito.when(idpayRestClient.getMerchantInitiativeList(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new RuntimeException()));

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
    void getMerchantInitiativeListTest_KOCertPermanent() {

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Mockito.when(idpayRestClient.getMerchantInitiativeList(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new CertificateException()));

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

        Assertions.assertNull(response.jsonPath().getList("initiatives"));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void getMerchantInitiativeListTest_KOCertAndOK() {

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Mockito.when(idpayRestClient.getMerchantInitiativeList(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new CertificateException()))
                .thenReturn(Uni.createFrom().item(initiativesList));

        Response response = given()
                .headers(validMilHeaders)
                .when()
                .get()
                .then()
                .extract()
                .response();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(6, response.jsonPath().getList("initiatives").size());
        Assertions.assertNull(response.jsonPath().getList("errors"));
    }
}
