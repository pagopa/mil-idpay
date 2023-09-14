package it.pagopa.swclient.mil.idpay;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.azurekeyvault.client.AzureKeyVaultClient;
import it.pagopa.swclient.mil.idpay.bean.CreateTransaction;
import it.pagopa.swclient.mil.idpay.bean.TransactionStatus;
import it.pagopa.swclient.mil.idpay.client.AzureADRestClient;
import it.pagopa.swclient.mil.idpay.client.IdpayAuthorizeTransactionRestClient;
import it.pagopa.swclient.mil.idpay.client.IdpayTransactionsRestClient;
import it.pagopa.swclient.mil.idpay.client.IpzsVerifyCieRestClient;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionResponse;
import it.pagopa.swclient.mil.idpay.client.bean.azure.AccessToken;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.IpzsVerifyCieResponse;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionEntity;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionRepository;
import it.pagopa.swclient.mil.idpay.resource.TransactionsResource;
import it.pagopa.swclient.mil.idpay.util.TransactionsTestData;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(TransactionsResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionResourceCIETest {

    @InjectMock
    @RestClient
    IdpayAuthorizeTransactionRestClient idpayAuthorizeTransactionRestClient;

    @InjectMock
    @RestClient
    IpzsVerifyCieRestClient ipzsVerifyCieRestClient;

    @InjectMock
    @RestClient
    AzureADRestClient azureADRestClient;

    @InjectMock
    @RestClient
    AzureKeyVaultClient azureKeyVaultClient;

    @InjectMock
    IdpayTransactionRepository idpayTransactionRepository;

    Map<String, String> validMilHeaders;

    String transactionId;
    CreateTransaction createTransactionRequest;
    IdpayTransactionEntity idpayTransactionEntity;
    TransactionResponse transactionResponse;
    IpzsVerifyCieResponse ipzsVerifyCieResponseOK;
    AccessToken azureAdAccessToken;
    
    @BeforeAll
    void createTestObjects() {
        validMilHeaders = TransactionsTestData.getMilHeaders();
        transactionResponse = TransactionsTestData.getCreatedTransaction();
        createTransactionRequest = TransactionsTestData.getCreateTransactionRequest();
        idpayTransactionEntity = TransactionsTestData.getTransactionEntity(validMilHeaders, createTransactionRequest, transactionResponse);
        transactionId = RandomStringUtils.random(32, true, true);

        ipzsVerifyCieResponseOK = TransactionsTestData.getIpzsVerifyCieResponseOK();
        azureAdAccessToken = TransactionsTestData.getAzureADAccessToken();
    }

    /*
    @Test
    void authorizeTransactionTest_OK() {

        Mockito.when(idpayAuthorizeTransactionRestClient.authorize(Mockito.any(String.class), Mockito.any(String.class), Mockito.any()))
                .thenReturn(Uni.createFrom().item(transactionResponse));

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));
    }*/

    @Test
    void getStatusTransactionTest_OK() {

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .pathParam("transactionId", transactionId)
                .when()
                .get("/{transactionId}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));

        Assertions.assertEquals(TransactionStatus.CREATED.toString(), response.jsonPath().getString("status"));
        Assertions.assertEquals("Updated Transaction for getStatusTransactionTest", response.jsonPath().getString("qrCode"));

    }
}
