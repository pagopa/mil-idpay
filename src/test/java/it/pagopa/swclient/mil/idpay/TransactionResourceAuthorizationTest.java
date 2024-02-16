package it.pagopa.swclient.mil.idpay;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.UnwrapKeyRequest;
import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.UnwrapKeyResponse;
import it.pagopa.swclient.mil.idpay.azurekeyvault.client.AzureKeyVaultClient;
import it.pagopa.swclient.mil.idpay.bean.*;
import it.pagopa.swclient.mil.idpay.bean.cer.CertificateBundle;
import it.pagopa.swclient.mil.idpay.bean.secret.SecretBundle;
import it.pagopa.swclient.mil.idpay.client.AzureADRestClient;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionResponse;
import it.pagopa.swclient.mil.idpay.client.bean.azure.AccessToken;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.IpzsVerifyCieResponse;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionEntity;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionRepository;
import it.pagopa.swclient.mil.idpay.resource.TransactionsResource;
import it.pagopa.swclient.mil.idpay.service.IdPayRestService;
import it.pagopa.swclient.mil.idpay.util.TransactionsTestData;
import jakarta.ws.rs.InternalServerErrorException;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.security.cert.CertificateException;
import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(TransactionsResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionResourceAuthorizationTest {


    @InjectMock
    IdPayRestService idPayRestService;

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
    UnwrapKeyResponse unwrapKeyResponse;
    AuthorizeTransaction authorizeTransaction;
    AuthTransactionResponse authTransactionResponse;
    PublicKeyIDPay publicKeyIDPay;
    CertificateBundle certificateBundle;
    SecretBundle secretBundle;

    @BeforeAll
    void createTestObjects() {
        validMilHeaders = TransactionsTestData.getMilHeaders();
        transactionResponse = TransactionsTestData.getIdentifiedTransaction();
        createTransactionRequest = TransactionsTestData.getCreateTransactionRequest();
        idpayTransactionEntity = TransactionsTestData.getTransactionEntity(validMilHeaders, createTransactionRequest, transactionResponse);
        transactionId = RandomStringUtils.random(32, true, true);

        ipzsVerifyCieResponseOK = TransactionsTestData.getIpzsVerifyCieResponseOK();
        azureAdAccessToken = TransactionsTestData.getAzureADAccessToken();
        unwrapKeyResponse = TransactionsTestData.getUnwrapKey();
        authorizeTransaction = TransactionsTestData.getAuthorizeTransaction();
        publicKeyIDPay = TransactionsTestData.getPublicKeyIdPay();
        authTransactionResponse = TransactionsTestData.getAuthTransactionResponse();
        certificateBundle = TransactionsTestData.getCertificateBundle();
        secretBundle = TransactionsTestData.getSecretBundle();
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void authorizeTransactionTest_OK() {

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn((Uni.createFrom().item(azureAdAccessToken)));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Mockito.when(azureKeyVaultClient.unwrapKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(UnwrapKeyRequest.class)))
                .thenReturn(Uni.createFrom().item(unwrapKeyResponse));

        Mockito.when(idPayRestService.retrieveIdpayPublicKey(Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(publicKeyIDPay));

        Mockito.when(idPayRestService.authorize(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(PinBlockDTO.class)))
                .thenReturn(Uni.createFrom().item(authTransactionResponse));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(authorizeTransaction)
                .pathParam("milTransactionId", transactionId)
                .when()
                .post("/{milTransactionId}/authorize")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void authorizeTransactionTest_KOIdPayFind500() {

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(500)));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(authorizeTransaction)
                .pathParam("milTransactionId", transactionId)
                .when()
                .post("/{milTransactionId}/authorize")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void authorizeTransactionTest_KOIdPayFind404() {

        idpayTransactionEntity = null;
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(authorizeTransaction)
                .pathParam("milTransactionId", transactionId)
                .when()
                .post("/{milTransactionId}/authorize")
                .then()
                .extract()
                .response();

        idpayTransactionEntity = TransactionsTestData.getTransactionEntity(validMilHeaders, createTransactionRequest, transactionResponse);

        Assertions.assertEquals(404, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_TRANSACTION_NOT_FOUND_MIL_DB));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void authorizeTransactionTest_KOTransactionNotIdentified() {

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.REWARDED);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(authorizeTransaction)
                .pathParam("milTransactionId", transactionId)
                .when()
                .post("/{milTransactionId}/authorize")
                .then()
                .extract()
                .response();

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.IDENTIFIED);

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_WRONG_TRANSACTION_STATUS_MIL_DB));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void authorizeTransactionTest_KOAccessToken500() {

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(500)));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(authorizeTransaction)
                .pathParam("milTransactionId", transactionId)
                .when()
                .post("/{milTransactionId}/authorize")
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
    void authorizeTransactionTest_KOUnwrapKey500() {

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.IDENTIFIED);
        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn((Uni.createFrom().item(azureAdAccessToken)));

        Mockito.when(azureKeyVaultClient.unwrapKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(UnwrapKeyRequest.class)))
                .thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(500)));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(authorizeTransaction)
                .pathParam("milTransactionId", transactionId)
                .when()
                .post("/{milTransactionId}/authorize")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_KEY_PAIR));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void authorizeTransactionTest_KORetrievePublicKey500() {

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn((Uni.createFrom().item(azureAdAccessToken)));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Mockito.when(azureKeyVaultClient.unwrapKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(UnwrapKeyRequest.class)))
                .thenReturn(Uni.createFrom().item(unwrapKeyResponse));

        Mockito.when(idPayRestService.retrieveIdpayPublicKey(Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(500)));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(authorizeTransaction)
                .pathParam("milTransactionId", transactionId)
                .when()
                .post("/{milTransactionId}/authorize")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_PUBLIC_KEY_IDPAY));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void authorizeTransactionTest_KOAuthorize500() {

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn((Uni.createFrom().item(azureAdAccessToken)));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Mockito.when(azureKeyVaultClient.unwrapKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(UnwrapKeyRequest.class)))
                .thenReturn(Uni.createFrom().item(unwrapKeyResponse));

        Mockito.when(idPayRestService.retrieveIdpayPublicKey(Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(publicKeyIDPay));

        Mockito.when(idPayRestService.authorize(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(PinBlockDTO.class)))
                .thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(500)));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(authorizeTransaction)
                .pathParam("milTransactionId", transactionId)
                .when()
                .post("/{milTransactionId}/authorize")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CALLING_AUTHORIZE_REST_SERVICES));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void authorizeTransactionTest_KOAuthorize400() {

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn((Uni.createFrom().item(azureAdAccessToken)));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Mockito.when(azureKeyVaultClient.unwrapKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(UnwrapKeyRequest.class)))
                .thenReturn(Uni.createFrom().item(unwrapKeyResponse));

        Mockito.when(idPayRestService.retrieveIdpayPublicKey(Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(publicKeyIDPay));

        Mockito.when(idPayRestService.authorize(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(PinBlockDTO.class)))
                .thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(403)));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(authorizeTransaction)
                .pathParam("milTransactionId", transactionId)
                .when()
                .post("/{milTransactionId}/authorize")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_IDPAY_PAYMENT_INVALID_PINBLOCK));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void authorizeTransactionTest_KOAuthorizeOther500() {

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn((Uni.createFrom().item(azureAdAccessToken)));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Mockito.when(azureKeyVaultClient.unwrapKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(UnwrapKeyRequest.class)))
                .thenReturn(Uni.createFrom().item(unwrapKeyResponse));

        Mockito.when(idPayRestService.retrieveIdpayPublicKey(Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(publicKeyIDPay));

        authTransactionResponse.setAuthTransactionResponseOk(null);
        authTransactionResponse.setAuthTransactionResponseWrong(null);

        Mockito.when(idPayRestService.authorize(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(PinBlockDTO.class)))
                .thenReturn(Uni.createFrom().item(authTransactionResponse));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(authorizeTransaction)
                .pathParam("milTransactionId", transactionId)
                .when()
                .post("/{milTransactionId}/authorize")
                .then()
                .extract()
                .response();

        authTransactionResponse = TransactionsTestData.getAuthTransactionResponse();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_IDPAY_UNKNOWN_ERROR_CODE));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void authorizeTransactionTest_KOEncryptingSessionKey() {

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn((Uni.createFrom().item(azureAdAccessToken)));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Mockito.when(azureKeyVaultClient.unwrapKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(UnwrapKeyRequest.class)))
                .thenReturn(Uni.createFrom().item(unwrapKeyResponse));

        publicKeyIDPay.setN("ABC");
        publicKeyIDPay.setE("DEF");
        Mockito.when(idPayRestService.retrieveIdpayPublicKey(Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(publicKeyIDPay));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(authorizeTransaction)
                .pathParam("milTransactionId", transactionId)
                .when()
                .post("/{milTransactionId}/authorize")
                .then()
                .extract()
                .response();

        publicKeyIDPay = TransactionsTestData.getPublicKeyIdPay();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_ENCRYPTING_SESSION_KEY));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void authorizeTransactionTest_KOAuthorizeUpdate500() {

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn((Uni.createFrom().item(azureAdAccessToken)));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Mockito.when(azureKeyVaultClient.unwrapKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(UnwrapKeyRequest.class)))
                .thenReturn(Uni.createFrom().item(unwrapKeyResponse));

        Mockito.when(idPayRestService.retrieveIdpayPublicKey(Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(publicKeyIDPay));

        Mockito.when(idPayRestService.authorize(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(PinBlockDTO.class)))
                .thenReturn(Uni.createFrom().item(authTransactionResponse));

        Mockito.when(idpayTransactionRepository.update(Mockito.any(IdpayTransactionEntity.class))).thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(500)));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(authorizeTransaction)
                .pathParam("milTransactionId", transactionId)
                .when()
                .post("/{milTransactionId}/authorize")
                .then()
                .extract()
                .response();

        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.IDENTIFIED);

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_STORING_DATA_IN_DB));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void authorizeTransactionTest_KOCertPermanent() {

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn((Uni.createFrom().item(azureAdAccessToken)));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Mockito.when(azureKeyVaultClient.unwrapKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(UnwrapKeyRequest.class)))
                .thenReturn(Uni.createFrom().item(unwrapKeyResponse));

        Mockito.when(idPayRestService.retrieveIdpayPublicKey(Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new CertificateException()));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(authorizeTransaction)
                .pathParam("milTransactionId", transactionId)
                .when()
                .post("/{milTransactionId}/authorize")
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
    void authorizeTransactionTest_KOCertAndOK() {
        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.IDENTIFIED);

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn((Uni.createFrom().item(azureAdAccessToken)));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Mockito.when(azureKeyVaultClient.unwrapKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(UnwrapKeyRequest.class)))
                .thenReturn(Uni.createFrom().item(unwrapKeyResponse));

        Mockito.when(idPayRestService.retrieveIdpayPublicKey(Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new CertificateException()))
                .thenReturn(Uni.createFrom().item(publicKeyIDPay));

        Mockito.when(idPayRestService.authorize(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(PinBlockDTO.class)))
                .thenReturn(Uni.createFrom().item(authTransactionResponse));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(authorizeTransaction)
                .pathParam("milTransactionId", transactionId)
                .when()
                .post("/{milTransactionId}/authorize")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"PayWithIDPay"})
    void authorizeTransactionTest_KOCertAndKO500() {
        idpayTransactionEntity.idpayTransaction.setStatus(TransactionStatus.IDENTIFIED);

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(azureADRestClient.getAccessToken(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn((Uni.createFrom().item(azureAdAccessToken)));

        Mockito.when(azureKeyVaultClient.getCertificate(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(certificateBundle));

        Mockito.when(azureKeyVaultClient.getSecret(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(secretBundle));

        Mockito.when(azureKeyVaultClient.unwrapKey(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(UnwrapKeyRequest.class)))
                .thenReturn(Uni.createFrom().item(unwrapKeyResponse));

        Mockito.when(idPayRestService.retrieveIdpayPublicKey(Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new CertificateException()))
                .thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(authorizeTransaction)
                .pathParam("milTransactionId", transactionId)
                .when()
                .post("/{milTransactionId}/authorize")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());
    }
}
