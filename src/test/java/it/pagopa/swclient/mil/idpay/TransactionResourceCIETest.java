package it.pagopa.swclient.mil.idpay;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.idpay.client.IdpayAuthorizeTransactionRestClient;
import it.pagopa.swclient.mil.idpay.client.IdpayTransactionsRestClient;
import it.pagopa.swclient.mil.idpay.resource.TransactionsResource;
import it.pagopa.swclient.mil.idpay.util.TransactionsTestData;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.util.Map;

@QuarkusTest
@TestHTTPEndpoint(TransactionsResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionResourceCIETest {

    @InjectMock
    @RestClient
    IdpayAuthorizeTransactionRestClient idpayAuthorizeTransactionRestClient;

    Map<String, String> validMilHeaders;
    
/*    @BeforeAll
    void createTestObjects() {
        validMilHeaders = TransactionsTestData.getMilHeaders();
        transactionResponse = TransactionsTestData.getCreatedTransaction();
        createTransactionRequest = TransactionsTestData.getCreateTransactionRequest();
        idpayTransactionEntity = TransactionsTestData.getTransactionEntity(validMilHeaders, createTransactionRequest, transactionResponse);
        idpayTransactionEntityForDelete = TransactionsTestData.getTransactionEntity(validMilHeaders, createTransactionRequest, transactionResponse);
        syncTrxStatus = TransactionsTestData.getStatusTransactionResponse();
        transactionId = RandomStringUtils.random(32, true, true);
    }

    @Test
    void authorizeTransactionTest_OK() {

        Mockito.when(idpayAuthorizeTransactionRestClient.authorize(Mockito.any(String.class), Mockito.any(String.class), Mockito.any()))
                .thenReturn(Uni.createFrom().item(transactionResponse));

        Mockito.when(idpayTransactionRepository.findById(Mockito.any(String.class))).thenReturn(Uni.createFrom().item(idpayTransactionEntity));
    }*/
}
