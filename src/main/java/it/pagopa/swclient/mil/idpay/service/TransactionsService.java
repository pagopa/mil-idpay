package it.pagopa.swclient.mil.idpay.service;

import io.quarkus.logging.Log;
import io.quarkus.runtime.LaunchMode;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.bean.Errors;
import it.pagopa.swclient.mil.idpay.ErrorCode;
import it.pagopa.swclient.mil.idpay.bean.CreateTransaction;
import it.pagopa.swclient.mil.idpay.bean.Transaction;
import it.pagopa.swclient.mil.idpay.client.IdpayTransactionsRestClient;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionCreationRequest;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionResponse;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransaction;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionEntity;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransactionsService {

    private SimpleDateFormat lastUpdateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Inject
    IdpayTransactionRepository idpayTransactionRepository;

    @RestClient
    IdpayTransactionsRestClient idpayTransactionsRestClient;

    public Uni<Transaction> createTransaction(CommonHeader headers, CreateTransaction createTransaction) {

        Log.debugf("TransactionsService -> createTransaction - Input parameters: %s, %s", headers, createTransaction);

        TransactionCreationRequest req = new TransactionCreationRequest();
        req.setInitiativeId(createTransaction.getInitiativeId());
        req.setAmountCents(createTransaction.getGoodsCost());
        req.setIdTrxAcquirer(UUID.randomUUID().toString());

        Log.debugf("TransactionsService -> createTransaction: REQUEST to idpay [%s]", req);


        return idpayTransactionsRestClient.createTransaction(headers.getMerchantId(), headers.getAcquirerId(), req)
                .onFailure().transform(t -> {
                    Log.errorf(t, "TransactionsService -> createTransaction: idpay error response for MerchantId [%s] e timestamp [%s]", headers.getMerchantId(), createTransaction.getTimestamp());
                    Errors errors = new Errors(List.of(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES), List.of(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES_MSG));
                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(errors)
                            .build());
                }).chain(res -> {
                    Log.debugf("TransactionsService -> createTransaction: idpay createTransaction service returned a 200 status, response: [%s]", res);

                    IdpayTransactionEntity entity = createIdpayTransactionEntity(headers, createTransaction, req, res);
                    Log.debugf("TransactionsService -> createTransaction: storing idpay transaction [%s] on DB", entity.idpayTransaction);

                    return idpayTransactionRepository.persist(entity)
                            .onFailure().transform(err-> {
                                Log.errorf(err, "TransactionsService -> createTransaction: Error while storing idpay transaction %s on db", entity.transactionId);
                                Errors errors = new Errors(List.of(ErrorCode.ERROR_STORING_DATA_IN_DB), List.of(ErrorCode.ERROR_STORING_DATA_IN_DB_MSG));
                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(errors)
                                        .build());
                            }).map(ent -> {
                                Transaction transaction = createTransactionFromIdpayTransactionEntity(ent);
                                return transaction;
                            });
                });
    }

    protected IdpayTransactionEntity createIdpayTransactionEntity(CommonHeader headers, CreateTransaction createTransaction, TransactionCreationRequest req, TransactionResponse res) {

        IdpayTransaction idpayTransaction = new IdpayTransaction();

        idpayTransaction.setAcquirerId(headers.getAcquirerId());
        idpayTransaction.setChannel(headers.getChannel());
        idpayTransaction.setMerchantId(res.getMerchantId());
        idpayTransaction.setTerminalId(headers.getTerminalId());
        idpayTransaction.setIdpayTransactionId(res.getId());

        if (LaunchMode.DEVELOPMENT.equals(io.quarkus.runtime.LaunchMode.current())) {
            idpayTransaction.setMilTransactionId(req.getIdTrxAcquirer());
        } else {
            idpayTransaction.setMilTransactionId(res.getIdTrxAcquirer());
        }

        idpayTransaction.setInitiativeId(res.getInitiativeId());
        idpayTransaction.setTimestamp(createTransaction.getTimestamp());
        idpayTransaction.setGoodsCost(createTransaction.getGoodsCost());
        idpayTransaction.setChallenge(null);
        idpayTransaction.setTrxCode(res.getTrxCode());
        idpayTransaction.setQrCode(null);
        idpayTransaction.setCoveredAmount(null);
        idpayTransaction.setStatus(res.getStatus());
        idpayTransaction.setLastUpdate(lastUpdateFormat.format(new Date()));

        IdpayTransactionEntity entity = new IdpayTransactionEntity();

        if (LaunchMode.DEVELOPMENT.equals(io.quarkus.runtime.LaunchMode.current())) {
            entity.transactionId = req.getIdTrxAcquirer();
        } else {
            entity.transactionId = res.getIdTrxAcquirer();
        }
        entity.idpayTransaction = idpayTransaction;

        return entity;
    }

    protected Transaction createTransactionFromIdpayTransactionEntity(IdpayTransactionEntity entity) {

        Transaction transaction = new Transaction();

        transaction.setIdpayTransactionId(entity.idpayTransaction.getIdpayTransactionId());
        transaction.setMilTransactionId(entity.idpayTransaction.getMilTransactionId());
        transaction.setInitiativeId(entity.idpayTransaction.getInitiativeId());
        transaction.setTimestamp(entity.idpayTransaction.getTimestamp());
        transaction.setGoodsCost(entity.idpayTransaction.getGoodsCost());
        transaction.setChallenge(entity.idpayTransaction.getChallenge());
        transaction.setTrxCode(entity.idpayTransaction.getTrxCode());
        transaction.setQrCode(entity.idpayTransaction.getQrCode());
        transaction.setCoveredAmount(entity.idpayTransaction.getCoveredAmount());
        transaction.setStatus(entity.idpayTransaction.getStatus());
        transaction.setLastUpdate(entity.idpayTransaction.getLastUpdate());

        return transaction;
    }
}
