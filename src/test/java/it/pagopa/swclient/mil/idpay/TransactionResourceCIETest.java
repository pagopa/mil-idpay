package it.pagopa.swclient.mil.idpay;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.DetailedKey;
import it.pagopa.swclient.mil.idpay.azurekeyvault.client.AzureKeyVaultClient;
import it.pagopa.swclient.mil.idpay.bean.CreateTransaction;
import it.pagopa.swclient.mil.idpay.bean.TransactionStatus;
import it.pagopa.swclient.mil.idpay.bean.VerifyCie;
import it.pagopa.swclient.mil.idpay.client.AzureADRestClient;
import it.pagopa.swclient.mil.idpay.client.IdpayAuthorizeTransactionRestClient;
import it.pagopa.swclient.mil.idpay.client.IpzsVerifyCieRestClient;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionResponse;
import it.pagopa.swclient.mil.idpay.client.bean.azure.AccessToken;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.IpzsVerifyCieResponse;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.Outcome;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionEntity;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionRepository;
import it.pagopa.swclient.mil.idpay.resource.TransactionsResource;
import it.pagopa.swclient.mil.idpay.util.TransactionsTestData;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeoutException;

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

    //valore vaultUrl deve coincidere con valore del parametro %test.quarkus.rest-client.azure-key-vault-api.url del application.properties
    private static final String vaultUrl = "https://156360cd-f617-4dcb-b908-ae29a2a8651c.mock.pstmn.io";
    Map<String, String> validMilHeaders;

    String transactionId;
    CreateTransaction createTransactionRequest;
    IdpayTransactionEntity idpayTransactionEntity;
    TransactionResponse transactionResponse;
    IpzsVerifyCieResponse ipzsVerifyCieResponseOK;
    AccessToken azureAdAccessToken;
    DetailedKey getKeyResponse;
    DetailedKey createKeyResponse;
    VerifyCie verifyCieRequest;
    
    @BeforeAll
    void createTestObjects() {
        validMilHeaders = TransactionsTestData.getMilHeaders();
        transactionResponse = TransactionsTestData.getCreatedTransaction();
        createTransactionRequest = TransactionsTestData.getCreateTransactionRequest();
        idpayTransactionEntity = TransactionsTestData.getTransactionEntity(validMilHeaders, createTransactionRequest, transactionResponse);
        transactionId = RandomStringUtils.random(32, true, true);

        ipzsVerifyCieResponseOK = TransactionsTestData.getIpzsVerifyCieResponseOK();
        azureAdAccessToken = TransactionsTestData.getAzureADAccessToken();

        getKeyResponse = TransactionsTestData.getAzureKVGetKeyResponse();
        verifyCieRequest = TransactionsTestData.getVerifyCieRequest();
        createKeyResponse = TransactionsTestData.getAzureKVCreateKeyResponse();
    }

    /*
    @Test
    void authorizeTransactionTest_OK() {

        Mockito.when(idpayAuthorizeTransactionRestClient.authorize(Mockito.any(String.class), Mockito.any(String.class), Mockito.any()))
                .thenReturn(Uni.createFrom().item(transactionResponse));

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));
    }*/

    @Test
    void verifyCIETest_OK() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));

        Assertions.assertEquals("RSA", response.jsonPath().getString("kty"));

    }

    @Test
    void verifyCIETest_KOIdpayStatus() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.IDENTIFIED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_WRONG_TRANSACTION_STATUS_MIL_DB));
    }

    @Test
    void verifyCIETest_KOIPZS404() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any()))
                .thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(404)));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_NOT_FOUND_IPZS_REST_SERVICES));
    }

    @Test
    void verifyCIETest_KOIPZS500() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any()))
                .thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(500)));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CALLING_IPZS_REST_SERVICES));
    }

    @Test
    void verifyCIETest_KOIPZSTimeout() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any()))
                .thenReturn(Uni.createFrom().failure(new TimeoutException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CALLING_IPZS_REST_SERVICES));
    }

    @Test
    void verifyCIETest_KOIPZSLost() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.LOST);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_LOST_IPZS_REST_SERVICES));

    }

    @Test
    void verifyCIETest_KOIPZSStolen() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.STOLEN);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_STOLEN_IPZS_REST_SERVICES));

    }

    @Test
    void verifyCIETest_KOIPZSExpired() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.EXPIRED);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_EXPIRED_IPZS_REST_SERVICES));

    }

    @Test
    void verifyCIETest_KOAzureAD() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new TimeoutException()));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CALLING_AZUREAD_REST_SERVICES));

    }

    @Test
    void verifyCIETest_KOAzureKVTimeout() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().failure(new TimeoutException()));



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_KEY_PAIR));
    }

    @Test
    void verifyCIETest_KOAzureKVTokenNull() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token(null);
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.AZUREAD_ACCESS_TOKEN_IS_NULL));
    }

    @Test
    void verifyCIETest_OK_AzureKV404ForCreateKey() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(404)));

        Mockito.when(azureKeyVaultClient.createKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(createKeyResponse));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));

        Assertions.assertEquals("RSA", response.jsonPath().getString("kty"));
    }

    @Test
    void verifyCIETest_KO_AzureKVCreateKey404() {
        //Log.debugf(">>>>>>>> START ");

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);

        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));


        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");

        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(404)));

        Mockito.when(azureKeyVaultClient.createKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(404)));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_KEY_PAIR));
    }

    @Test
    void verifyCIETest_KOAzureKeyInvalid() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        getKeyResponse.getDetails().setKty("DSA");
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        getKeyResponse.getDetails().setKty("RSA");

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_KEY_PAIR));

    }


    @Test
    void verifyCIETest_KOAzureKeyVersionInvalid() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        getKeyResponse.getDetails().setKid(vaultUrl + "/keys/0709643f49394529b92c19a68c8e184a");
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        getKeyResponse = TransactionsTestData.getAzureKVGetKeyResponse();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));

        Assertions.assertEquals("RSA", response.jsonPath().getString("kty"));
    }


    @Test
    void verifyCIETest_KOAzureKVKidNull() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        getKeyResponse.getDetails().setKid(null);
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        getKeyResponse = TransactionsTestData.getAzureKVGetKeyResponse();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));

        Assertions.assertEquals("RSA", response.jsonPath().getString("kty"));
    }


    @Test
    void verifyCIETest_KOAzureKVKeyOpsNull() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        getKeyResponse.getDetails().setKeyOps(null);
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        getKeyResponse = TransactionsTestData.getAzureKVGetKeyResponse();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_KEY_PAIR));

    }

    @Test
    void verifyCIETest_KOAzureKVKeyOpsWrap() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        String[] keyOps = new String[] {
                "wrapKey"
        };

        getKeyResponse.getDetails().setKeyOps(keyOps);
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        getKeyResponse = TransactionsTestData.getAzureKVGetKeyResponse();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_KEY_PAIR));

    }

    @Test
    void verifyCIETest_KOAzureKVKeyOpsUnwrap() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        String[] keyOps = new String[] {
                "unwrapKey"
        };

        getKeyResponse.getDetails().setKeyOps(keyOps);
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        getKeyResponse = TransactionsTestData.getAzureKVGetKeyResponse();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_KEY_PAIR));

    }

    @Test
    void verifyCIETest_KOAzureKVKeyAttrNull() {
        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        DetailedKey kr = TransactionsTestData.getAzureKVGetKeyResponseAttrNull();
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(kr));



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        //getKeyResponse = TransactionsTestData.getAzureKVGetKeyResponse();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_KEY_PAIR));
    }

    @Test
    void verifyCIETest_KOAzureKVKeyEnabledNull() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        getKeyResponse.getAttributes().setEnabled(null);
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        getKeyResponse.getAttributes().setEnabled(true);

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_KEY_PAIR));

    }

    @Test
    void verifyCIETest_KOAzureKVKeyEnabledFalse() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        getKeyResponse.getAttributes().setEnabled(false);
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        getKeyResponse.getAttributes().setEnabled(true);

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_KEY_PAIR));
    }

    @Test
    void verifyCIETest_KOAzureKVKeyCreatedInFuture() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        long now = Instant.now().getEpochSecond();

        getKeyResponse.getAttributes().setCreated(now + 1000);
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        getKeyResponse.getAttributes().setCreated(now - 300);

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_KEY_PAIR));
    }

    @Test
    void verifyCIETest_KOAzureKVKeyCreatedNull() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        getKeyResponse.getAttributes().setCreated(null);
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        long now = Instant.now().getEpochSecond();
        getKeyResponse.getAttributes().setCreated(now - 300);

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_KEY_PAIR));
    }

    @Test
    void verifyCIETest_KOAzureKVKeyExpired() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        long now = Instant.now().getEpochSecond();

        getKeyResponse.getAttributes().setExp(now - 1000);
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));

        Mockito.when(azureKeyVaultClient.createKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(createKeyResponse));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        getKeyResponse.getAttributes().setExp(now + 600);
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));

        Assertions.assertEquals("RSA", response.jsonPath().getString("kty"));
    }

    @Test
    void verifyCIETest_KOAzureKVKeyExpiredNull() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        long now = Instant.now().getEpochSecond();

        getKeyResponse.getAttributes().setExp(null);
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));

        Mockito.when(azureKeyVaultClient.createKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(createKeyResponse));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        getKeyResponse.getAttributes().setExp(now + 600);

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));

        Assertions.assertEquals("RSA", response.jsonPath().getString("kty"));
    }

    @Test
    void verifyCIETest_KOAzureKVForbidden() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(403)));



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_KEY_PAIR));
    }

    @Test
    void verifyCIETest_KOAzureKVGetKeyNullResponse() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().nullItem());



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_KEY_PAIR));
    }

    @Test
    void verifyCIETest_OK_NoPOS() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));

        Map<String, String> validMilHeadersNonPOS = TransactionsTestData.getMilHeadersNonPOS();

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeadersNonPOS)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));

        Assertions.assertEquals("RSA", response.jsonPath().getString("kty"));
    }

    @Test
    void verifyCIETest_KO_AzureKVCreateKeyDSA() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(404)));

        createKeyResponse.getDetails().setKty("DSA");
        Mockito.when(azureKeyVaultClient.createKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(createKeyResponse));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        createKeyResponse.getDetails().setKty("RSA");

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_KEY_PAIR));
    }

    /*
    @Test
    void verifyCIETest_KOAzureKeyNameVersionInvalid() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        getKeyResponse.getKey().setKid(vaultUrl + "/keys/");
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));



        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        getKeyResponse = TransactionsTestData.getAzureKVGetKeyResponse();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_KEY_PAIR));

    }*/

    @Test
    void verifyCIETest_KOAzureKVNotBeforeFuture() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        long now = Instant.now().getEpochSecond();

        getKeyResponse.getAttributes().setNbf(now + 1000);
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));

        Mockito.when(azureKeyVaultClient.createKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(createKeyResponse));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        getKeyResponse.getAttributes().setNbf(now - 300);
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));

        Assertions.assertEquals("RSA", response.jsonPath().getString("kty"));
    }

    @Test
    void verifyCIETest_KOAzureKVNotBeforeNull() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        long now = Instant.now().getEpochSecond();

        getKeyResponse.getAttributes().setNbf(null);
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));

        Mockito.when(azureKeyVaultClient.createKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(createKeyResponse));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        getKeyResponse.getAttributes().setNbf(now - 300);
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));

        Assertions.assertEquals("RSA", response.jsonPath().getString("kty"));
    }

    @Test
    void verifyCIETest_KOAzureKVKeyDetailsNull() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.CREATED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        ipzsVerifyCieResponseOK.setOutcome(Outcome.OK);
        Mockito.when(ipzsVerifyCieRestClient.identitycards(Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(ipzsVerifyCieResponseOK));

        azureAdAccessToken.setAccess_token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9.eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsIm5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdlQnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0ZKNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLWJlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyMjJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29tIiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDVyclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMGVRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt");
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(azureAdAccessToken));

        getKeyResponse.setDetails(null);
        Mockito.when(azureKeyVaultClient.getKey(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Uni.createFrom().item(getKeyResponse));

        Mockito.when(azureKeyVaultClient.createKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any())).thenReturn(Uni.createFrom().item(createKeyResponse));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(verifyCieRequest)
                .pathParam("transactionId", transactionId)
                .when()
                .post("/{transactionId}/verifyCie")
                .then()
                .extract()
                .response();

        getKeyResponse = TransactionsTestData.getAzureKVGetKeyResponse();
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));

        Assertions.assertEquals("RSA", response.jsonPath().getString("kty"));
    }
}
