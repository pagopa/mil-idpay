package it.pagopa.swclient.mil.idpay;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.bean.CreateTransaction;
import it.pagopa.swclient.mil.idpay.bean.TransactionStatus;
import it.pagopa.swclient.mil.idpay.client.IdpayTransactionsRestClient;
import it.pagopa.swclient.mil.idpay.client.bean.PreAuthPaymentResponseDTO;
import it.pagopa.swclient.mil.idpay.client.bean.SyncTrxStatus;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionResponse;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(TransactionsResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionsResourceTest {

    @InjectMock
    @RestClient
    IdpayTransactionsRestClient idpayTransactionsRestClient;

    @InjectMock
    IdpayTransactionRepository idpayTransactionRepository;

    Map<String, String> validMilHeaders;

    TransactionResponse transactionResponse;

    CreateTransaction createTransactionRequest;

    IdpayTransactionEntity idpayTransactionEntity;

    IdpayTransactionEntity idpayTransactionEntityForDelete;

    SyncTrxStatus syncTrxStatus;

    SyncTrxStatus syncTrxStatusNotChanged;

    String transactionId;

    //List<IdpayTransactionEntity> transactionEntityList;
    PreAuthPaymentResponseDTO preAuthPaymentResponseDTO;

    @BeforeAll
    void createTestObjects() {
        validMilHeaders = TransactionsTestData.getMilHeaders();
        transactionResponse = TransactionsTestData.getCreatedTransaction();
        createTransactionRequest = TransactionsTestData.getCreateTransactionRequest();
        idpayTransactionEntity = TransactionsTestData.getTransactionEntity(validMilHeaders, createTransactionRequest, transactionResponse);
        idpayTransactionEntityForDelete = TransactionsTestData.getTransactionEntity(validMilHeaders, createTransactionRequest, transactionResponse);
        syncTrxStatus = TransactionsTestData.getStatusTransactionResponse();
        transactionId = RandomStringUtils.random(32, true, true);
        syncTrxStatusNotChanged = TransactionsTestData.getStatusTransactionResponseNotChanged();
        //transactionEntityList = TransactionsTestData.getListTransactionEntity(validMilHeaders, createTransactionRequest, transactionResponse);
        preAuthPaymentResponseDTO = TransactionsTestData.getSecondFactor();
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void createTransactionTest_OK() {

        Mockito.when(idpayTransactionsRestClient.createTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any()))
                .thenReturn(Uni.createFrom().item(transactionResponse));

        Mockito.when(idpayTransactionRepository.persist(Mockito.any(IdpayTransactionEntity.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(createTransactionRequest)
                .when()
                .post("/")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(201, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));

        Assertions.assertEquals(TransactionStatus.CREATED.toString(), response.jsonPath().getString("status"));

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void createTransactionTestTest_KO500() {

        Mockito.when(idpayTransactionsRestClient.createTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any()))
                .thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(500)));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(createTransactionRequest)
                .when()
                .post("/")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());
        Assertions.assertNull(response.jsonPath().getList("transaction"));
    }


    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void createTransactionTest_KOPersist() {

        Mockito.when(idpayTransactionsRestClient.createTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any()))
                .thenReturn(Uni.createFrom().item(transactionResponse));

        Mockito.when(idpayTransactionRepository.persist(Mockito.any(IdpayTransactionEntity.class))).thenReturn(Uni.createFrom().failure(new TimeoutException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .body(createTransactionRequest)
                .when()
                .post("/")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());
        Assertions.assertNull(response.jsonPath().getList("transaction"));

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_STORING_DATA_IN_DB));

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void getStatusTransactionTest_OK() {

        Mockito.when(idpayTransactionsRestClient.getStatusTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(syncTrxStatus));

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        IdpayTransactionEntity updEntity = idpayTransactionEntity;
        //updEntity.idpayTransaction.setTrxCode("Updated Transaction for getStatusTransactionTest");

        Mockito.when(idpayTransactionRepository.update(Mockito.any(IdpayTransactionEntity.class))).thenReturn(Uni.createFrom().item(updEntity));

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
        //Assertions.assertEquals("Updated Transaction for getStatusTransactionTest", response.jsonPath().getString("trxCode"));

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void getStatusTransactionTest_OK_notChanged() {

        idpayTransactionEntity = TransactionsTestData.getTransactionEntity(validMilHeaders, createTransactionRequest, transactionResponse);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(idpayTransactionsRestClient.getStatusTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(syncTrxStatusNotChanged));

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

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void getStatusTransactionTest_KOFind() {

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().failure(new TimeoutException()));

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

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB));

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void getStatusTransactionTest_KOFindNull() {

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().nullItem());

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

        Assertions.assertEquals(404, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_TRANSACTION_NOT_FOUND_MIL_DB));

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void getStatusTransactionTest_KOIdpay404() {

        Mockito.when(idpayTransactionsRestClient.getStatusTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(404)));

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

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

        Assertions.assertEquals(404, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_NOT_FOUND_IDPAY_REST_SERVICES));

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void getStatusTransactionTest_KOIdpay500() {

        Mockito.when(idpayTransactionsRestClient.getStatusTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(500)));

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

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

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES));

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void getStatusTransactionTest_KOIdpay() {

        Mockito.when(idpayTransactionsRestClient.getStatusTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new TimeoutException()));

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

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

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES));

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void getStatusTransactionTest_KOUpdate() {
        syncTrxStatus.setStatus(TransactionStatus.AUTHORIZED);
        Mockito.when(idpayTransactionsRestClient.getStatusTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(syncTrxStatus));

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        Mockito.when(idpayTransactionRepository.update(Mockito.any(IdpayTransactionEntity.class))).thenReturn(Uni.createFrom().failure(new TimeoutException()));

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

        syncTrxStatus.setStatus(TransactionStatus.CREATED);
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));
    }


    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void deleteTransactionTest_OK() {

        Mockito.when(idpayTransactionsRestClient.deleteTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().voidItem());

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntityForDelete));

        IdpayTransactionEntity updEntity = idpayTransactionEntityForDelete;
        updEntity.idpayTransaction.setTrxCode("Updated Transaction for deleteTransactionTest_OK");

        Mockito.when(idpayTransactionRepository.update(Mockito.any(IdpayTransactionEntity.class))).thenReturn(Uni.createFrom().item(updEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .pathParam("transactionId", transactionId)
                .when()
                .delete("/{transactionId}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(204, response.statusCode());

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void deleteTransactionTest_KOFind() {

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().failure(new TimeoutException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .pathParam("transactionId", transactionId)
                .when()
                .delete("/{transactionId}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB));

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void deleteTransactionTest_KOFindNull() {

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().nullItem());

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .pathParam("transactionId", transactionId)
                .when()
                .delete("/{transactionId}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(404, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_TRANSACTION_NOT_FOUND_MIL_DB));

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void deleteTransactionTest_KOIdpay404() {

        Mockito.when(idpayTransactionsRestClient.deleteTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(404)));

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntityForDelete));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .pathParam("transactionId", transactionId)
                .when()
                .delete("/{transactionId}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(404, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_NOT_FOUND_IDPAY_REST_SERVICES));

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void deleteTransactionTest_KOIdpay500() {

        Mockito.when(idpayTransactionsRestClient.deleteTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(500)));

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntityForDelete));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .pathParam("transactionId", transactionId)
                .when()
                .delete("/{transactionId}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES));

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void deleteTransactionTest_KOIdpay() {

        Mockito.when(idpayTransactionsRestClient.deleteTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new TimeoutException()));

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntityForDelete));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .pathParam("transactionId", transactionId)
                .when()
                .delete("/{transactionId}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES));

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void deleteTransactionTest_KOUpdate() {

        Mockito.when(idpayTransactionsRestClient.deleteTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().voidItem());

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntityForDelete));

        IdpayTransactionEntity updEntity = idpayTransactionEntityForDelete;
        updEntity.idpayTransaction.setTrxCode("Updated Transaction for deleteTransactionTest_KOUpdate");

        Mockito.when(idpayTransactionRepository.update(Mockito.any(IdpayTransactionEntity.class))).thenReturn(Uni.createFrom().failure(new TimeoutException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .and()
                .pathParam("transactionId", transactionId)
                .when()
                .delete("/{transactionId}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(204, response.statusCode());

    }

    //Storico
    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void getLastTransactionsTest_OK() {

        ReactivePanacheQuery<IdpayTransactionEntity> reactivePanacheQuery = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(reactivePanacheQuery.page(Mockito.any())).thenReturn(reactivePanacheQuery);
        Mockito.when(reactivePanacheQuery.withBatchSize(Mockito.anyInt())).thenReturn(reactivePanacheQuery);
        Mockito.when(reactivePanacheQuery.list()).thenReturn(Uni.createFrom().item(List.of(idpayTransactionEntity)));

        Mockito
                .when(idpayTransactionRepository.find(Mockito.anyString(), Mockito.any(Sort.class), Mockito.any(Object[].class)))
                .thenReturn(reactivePanacheQuery);

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .when()
                .get("/")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));

        Assertions.assertEquals(1, response.jsonPath().getList("transactions").size());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void getLastTransactionsTest_KO_DB() {

        ReactivePanacheQuery<IdpayTransactionEntity> reactivePanacheQuery = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(reactivePanacheQuery.page(Mockito.any())).thenReturn(reactivePanacheQuery);
        Mockito.when(reactivePanacheQuery.withBatchSize(Mockito.anyInt())).thenReturn(reactivePanacheQuery);
        Mockito.when(reactivePanacheQuery.list()).thenReturn(Uni.createFrom().failure(new TimeoutException()));

        Mockito
                .when(idpayTransactionRepository.find(Mockito.anyString(), Mockito.any(Sort.class), Mockito.any(Object[].class)))
                .thenReturn(reactivePanacheQuery);

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(validMilHeaders)
                .when()
                .get("/")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB));
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void getStatusTransactionSecondFactorTest_OK() {

        syncTrxStatus.setStatus(TransactionStatus.IDENTIFIED);
        Mockito.when(idpayTransactionsRestClient.getStatusTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(syncTrxStatus));

        idpayTransactionEntity.idpayTransaction.setByCie(true);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        IdpayTransactionEntity updEntity = idpayTransactionEntity;
        //updEntity.idpayTransaction.setTrxCode("Updated Transaction for getStatusTransactionTest");

        Mockito.when(idpayTransactionRepository.update(Mockito.any(IdpayTransactionEntity.class))).thenReturn(Uni.createFrom().item(updEntity));

        Mockito.when(idpayTransactionsRestClient.putPreviewPreAuthPayment(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(preAuthPaymentResponseDTO));

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

        syncTrxStatus.setStatus(TransactionStatus.CREATED);
        idpayTransactionEntity.idpayTransaction.setByCie(null);

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));

        Assertions.assertEquals(TransactionStatus.IDENTIFIED.toString(), response.jsonPath().getString("status"));
        Assertions.assertNotNull(response.jsonPath().getString("secondFactor"));

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void getStatusTransactionSecondFactorTest_KOIdPay() {

        syncTrxStatus.setStatus(TransactionStatus.IDENTIFIED);
        Mockito.when(idpayTransactionsRestClient.getStatusTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(syncTrxStatus));

        idpayTransactionEntity.idpayTransaction.setByCie(true);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        IdpayTransactionEntity updEntity = idpayTransactionEntity;
        //updEntity.idpayTransaction.setTrxCode("Updated Transaction for getStatusTransactionTest");

        Mockito.when(idpayTransactionRepository.update(Mockito.any(IdpayTransactionEntity.class))).thenReturn(Uni.createFrom().item(updEntity));

        Mockito.when(idpayTransactionsRestClient.putPreviewPreAuthPayment(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(500)));

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

        syncTrxStatus.setStatus(TransactionStatus.CREATED);
        idpayTransactionEntity.idpayTransaction.setByCie(null);

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertEquals(1, response.jsonPath().getList("descriptions").size());

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void getStatusTransactionSecondFactorTestCieNull_OK() {

        syncTrxStatus.setStatus(TransactionStatus.IDENTIFIED);
        Mockito.when(idpayTransactionsRestClient.getStatusTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(syncTrxStatus));

        //idpayTransactionEntity.idpayTransaction.setByCie(true);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        IdpayTransactionEntity updEntity = idpayTransactionEntity;
        //updEntity.idpayTransaction.setTrxCode("Updated Transaction for getStatusTransactionTest");

        Mockito.when(idpayTransactionRepository.update(Mockito.any(IdpayTransactionEntity.class))).thenReturn(Uni.createFrom().item(updEntity));

        Mockito.when(idpayTransactionsRestClient.putPreviewPreAuthPayment(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(preAuthPaymentResponseDTO));

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

        syncTrxStatus.setStatus(TransactionStatus.CREATED);
        //idpayTransactionEntity.idpayTransaction.setByCie(null);

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));

        Assertions.assertEquals(TransactionStatus.IDENTIFIED.toString(), response.jsonPath().getString("status"));
        Assertions.assertNull(response.jsonPath().getString("secondFactor"));

    }

    @Test
    @TestSecurity(user = "testUser", roles = { "PayWithIDPay" })
    void getStatusTransactionSecondFactorTestCieFalse_OK() {

        syncTrxStatus.setStatus(TransactionStatus.IDENTIFIED);
        Mockito.when(idpayTransactionsRestClient.getStatusTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(syncTrxStatus));

        idpayTransactionEntity.idpayTransaction.setByCie(false);
        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));

        IdpayTransactionEntity updEntity = idpayTransactionEntity;
        //updEntity.idpayTransaction.setTrxCode("Updated Transaction for getStatusTransactionTest");

        Mockito.when(idpayTransactionRepository.update(Mockito.any(IdpayTransactionEntity.class))).thenReturn(Uni.createFrom().item(updEntity));

        Mockito.when(idpayTransactionsRestClient.putPreviewPreAuthPayment(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(preAuthPaymentResponseDTO));

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

        syncTrxStatus.setStatus(TransactionStatus.CREATED);
        idpayTransactionEntity.idpayTransaction.setByCie(null);

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.jsonPath().getList("errors"));

        Assertions.assertEquals(TransactionStatus.IDENTIFIED.toString(), response.jsonPath().getString("status"));
        Assertions.assertNull(response.jsonPath().getString("secondFactor"));

    }
}
