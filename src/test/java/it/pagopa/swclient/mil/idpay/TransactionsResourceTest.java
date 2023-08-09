package it.pagopa.swclient.mil.idpay;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.bean.Errors;
import it.pagopa.swclient.mil.idpay.bean.CreateTransaction;
import it.pagopa.swclient.mil.idpay.bean.TransactionStatus;
import it.pagopa.swclient.mil.idpay.client.IdpayTransactionsRestClient;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionResponse;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionEntity;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionRepository;
import it.pagopa.swclient.mil.idpay.resource.TransactionsResource;
import it.pagopa.swclient.mil.idpay.util.TransactionsTestData;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.util.Map;
import java.util.concurrent.TimeoutException;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(TransactionsResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionsResourceTest {

    @InjectMock
    @RestClient
    IdpayTransactionsRestClient idpayTransactionsRestClient;

    @InjectMock
    IdpayTransactionRepository idpayTransactionRepository;

    Map<String, String> validMilHeaders;

    TransactionResponse transactionResponse;

    CreateTransaction createTransactionRequest;

    IdpayTransactionEntity idpayTransactionEntity;
    Errors errors;

    @BeforeAll
    void createTestObjects() {
        validMilHeaders = TransactionsTestData.getMilHeaders();
        transactionResponse = TransactionsTestData.getCreatedTransaction();
        createTransactionRequest = TransactionsTestData.getCreateTransactionRequest();
        idpayTransactionEntity = TransactionsTestData.getTransactionEntity(validMilHeaders, createTransactionRequest, transactionResponse);

    }

    @Test
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

/*
    @Test
    void createTransactionTest_KORestRespElab() {

        Mockito.when(idpayTransactionsRestClient.createTransaction(Mockito.any(String.class), Mockito.any(String.class), Mockito.any()))
                .thenReturn(Uni.createFrom().nullItem());

        //Mockito.when(idpayTransactionRepository.persist(Mockito.any(IdpayTransactionEntity.class))).thenReturn(Uni.createFrom().failure(new TimeoutException()));

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

    }*/

}
