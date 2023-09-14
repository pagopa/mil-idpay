package it.pagopa.swclient.mil.idpay.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.bean.Errors;
import it.pagopa.swclient.mil.idpay.ErrorCode;
import it.pagopa.swclient.mil.idpay.bean.CreateTransaction;
import it.pagopa.swclient.mil.idpay.bean.CreateTransactionResp;
import it.pagopa.swclient.mil.idpay.bean.Transaction;
import it.pagopa.swclient.mil.idpay.bean.TransactionStatus;
import it.pagopa.swclient.mil.idpay.client.IdpayTransactionsRestClient;
import it.pagopa.swclient.mil.idpay.client.bean.SyncTrxStatus;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionCreationRequest;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionResponse;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransaction;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionEntity;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransactionsService {

    private final SimpleDateFormat lastUpdateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Inject
    IdpayTransactionRepository idpayTransactionRepository;

    @RestClient
    IdpayTransactionsRestClient idpayTransactionsRestClient;

    public Uni<CreateTransactionResp> createTransaction(CommonHeader headers, CreateTransaction createTransaction) {

        Log.debugf("TransactionsService -> createTransaction - Input parameters: %s, %s", headers, createTransaction);

        TransactionCreationRequest req = new TransactionCreationRequest();
        req.setInitiativeId(createTransaction.getInitiativeId());
        req.setAmountCents(createTransaction.getGoodsCost());
        req.setIdTrxAcquirer(UUID.randomUUID().toString());

        Log.debugf("TransactionsService -> createTransaction: REQUEST to idpay [%s]", req);

        return idpayTransactionsRestClient.createTransaction(getIdpayMerchantId(headers.getMerchantId(), headers.getAcquirerId()), headers.getAcquirerId(), req)
                .onFailure().transform(t -> {
                    Log.errorf(t, "TransactionsService -> createTransaction: idpay error response for MerchantId [%s] e timestamp [%s]", headers.getMerchantId(), createTransaction.getTimestamp());

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES), List.of(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES_MSG)))
                            .build());
                }).chain(res -> {
                    Log.debugf("TransactionsService -> createTransaction: idpay createTransaction service returned a 200 status, response: [%s]", res);

                    IdpayTransactionEntity entity = createIdpayTransactionEntity(headers, createTransaction, req, res);
                    Log.debugf("TransactionsService -> createTransaction: storing idpay transaction [%s] on DB", entity.idpayTransaction);

                    return idpayTransactionRepository.persist(entity)
                            .onFailure().transform(err-> {
                                Log.errorf(err, "TransactionsService -> createTransaction: Error while storing transaction %s on db", entity.transactionId);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(List.of(ErrorCode.ERROR_STORING_DATA_IN_DB), List.of(ErrorCode.ERROR_STORING_DATA_IN_DB_MSG)))
                                        .build());
                            }).map(ent -> createTransactionRespFromIdpayTransactionEntity(ent, res.getQrcodeTxtUrl()));
                });
    }

    protected IdpayTransactionEntity createIdpayTransactionEntity(CommonHeader headers, CreateTransaction createTransaction, TransactionCreationRequest req, TransactionResponse res) {

        IdpayTransaction idpayTransaction = new IdpayTransaction();

        idpayTransaction.setMilTransactionId(req.getIdTrxAcquirer());
        idpayTransaction.setAcquirerId(res.getAcquirerId());
        idpayTransaction.setChannel(headers.getChannel());
        idpayTransaction.setMerchantId(headers.getMerchantId());
        idpayTransaction.setTerminalId(headers.getTerminalId());
        idpayTransaction.setIdpayMerchantId(res.getMerchantId());
        idpayTransaction.setIdpayTransactionId(res.getId());
        idpayTransaction.setInitiativeId(res.getInitiativeId());
        idpayTransaction.setTimestamp(createTransaction.getTimestamp());
        idpayTransaction.setGoodsCost(createTransaction.getGoodsCost());
        idpayTransaction.setTrxCode(res.getTrxCode());
        idpayTransaction.setStatus(res.getStatus());

        IdpayTransactionEntity entity = new IdpayTransactionEntity();

        entity.transactionId = req.getIdTrxAcquirer();

        entity.idpayTransaction = idpayTransaction;

        return entity;
    }

    protected CreateTransactionResp createTransactionRespFromIdpayTransactionEntity(IdpayTransactionEntity entity, String qrCode) {

        CreateTransactionResp transaction = new CreateTransactionResp();

        transaction.setIdpayTransactionId(entity.idpayTransaction.getIdpayTransactionId());
        transaction.setMilTransactionId(entity.idpayTransaction.getMilTransactionId());
        transaction.setInitiativeId(entity.idpayTransaction.getInitiativeId());
        transaction.setTimestamp(entity.idpayTransaction.getTimestamp());
        transaction.setGoodsCost(entity.idpayTransaction.getGoodsCost());
        transaction.setChallenge(entity.idpayTransaction.getTrxCode().getBytes(StandardCharsets.UTF_8));
        transaction.setTrxCode(entity.idpayTransaction.getTrxCode());
        transaction.setQrCode(qrCode);
        transaction.setStatus(entity.idpayTransaction.getStatus());

        return transaction;
    }

    public Uni<Transaction> getTransaction(CommonHeader headers, String transactionId) {

        Log.debugf("TransactionsService -> getTransaction - Input parameters: %s, %s", headers, transactionId);

        return idpayTransactionRepository.findById(transactionId) //looking for MilTransactionID in DB
                .onFailure().transform(t -> {
                    Log.errorf(t, "[%s] TransactionsService -> getTransaction: Error while retrieving mil transaction [%s] from DB",
                            ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB, transactionId);
                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB), List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB_MSG)))
                            .build());
                })
                .onItem().ifNull().failWith(() -> {
                    // if no transaction is found return TRANSACTION_NOT_FOUND
                    Log.errorf("TransactionsService -> getTransaction: transaction [%s] not found on mil DB", transactionId);

                    return new NotFoundException(Response
                            .status(Response.Status.NOT_FOUND)
                            .entity(new Errors(List.of(ErrorCode.ERROR_TRANSACTION_NOT_FOUND_MIL_DB), List.of(ErrorCode.ERROR_TRANSACTION_NOT_FOUND_MIL_DB_MSG)))
                            .build());
                }).chain(entity -> { //Transaction found
                    Log.debugf("TransactionsService -> getTransaction: found idpay transaction [%s] for mil transaction [%s]", entity.idpayTransaction.getIdpayTransactionId(), transactionId);

                    //call idpay to retrieve current state
                    return idpayTransactionsRestClient.getStatusTransaction(entity.idpayTransaction.getIdpayMerchantId(), headers.getAcquirerId(), entity.idpayTransaction.getIdpayTransactionId())
                            .onFailure().transform(t -> {
                                Log.errorf(t, "TransactionsService -> getTransaction: idpay error response for idpay transaction [%s] for mil transaction [%s]", entity.idpayTransaction.getIdpayTransactionId(), transactionId);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(List.of(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES), List.of(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES_MSG)))
                                        .build());
                            }).chain(res -> { //response ok
                                Log.debugf("TransactionsService -> getTransaction: idpay getStatusTransaction service returned a 200 status, response: [%s]", res);

                                IdpayTransactionEntity updEntity = updateIdpayTransactionEntity(headers, entity, res);

                                if (updEntity.idpayTransaction.equals(entity.idpayTransaction)) {
                                    Log.debugf("TransactionsService -> getTransaction: transaction situation is NOT changed");
                                    return Uni.createFrom().item(createTransactionFromIdpayTransactionEntity(updEntity, res.getSecondFactor()));
                                } else {
                                    Log.debugf("TransactionsService -> getTransaction: transaction situation is changed, make an update");
                                    return idpayTransactionRepository.update(updEntity) //updating transaction in DB mil
                                            .onFailure().recoverWithItem(err -> {
                                                Log.errorf(err, "TransactionsService -> getTransaction: Error while updating transaction %s on db", entity.transactionId);

                                                return updEntity;
                                            }).map(ent -> createTransactionFromIdpayTransactionEntity(ent, res.getSecondFactor()));//update ok, send response to a client
                                }
                            });
                });
    }

    protected IdpayTransactionEntity updateIdpayTransactionEntity(CommonHeader headers, IdpayTransactionEntity entity, SyncTrxStatus res) {

        IdpayTransaction idpayTransaction = new IdpayTransaction();

        idpayTransaction.setMilTransactionId(entity.idpayTransaction.getMilTransactionId());
        idpayTransaction.setAcquirerId(res.getAcquirerId());
        idpayTransaction.setChannel(headers.getChannel());
        idpayTransaction.setMerchantId(headers.getMerchantId());
        idpayTransaction.setTerminalId(headers.getTerminalId());
        idpayTransaction.setIdpayMerchantId(res.getMerchantId());
        idpayTransaction.setIdpayTransactionId(res.getId());
        idpayTransaction.setInitiativeId(res.getInitiativeId());
        idpayTransaction.setTimestamp(entity.idpayTransaction.getTimestamp());
        idpayTransaction.setGoodsCost(entity.idpayTransaction.getGoodsCost());
        idpayTransaction.setTrxCode(res.getTrxCode());
        idpayTransaction.setStatus(res.getStatus());
        idpayTransaction.setCoveredAmount(res.getRewardCents());
        idpayTransaction.setLastUpdate(lastUpdateFormat.format(new Date()));

        IdpayTransactionEntity trEntity = new IdpayTransactionEntity();

        trEntity.transactionId = entity.transactionId;

        trEntity.idpayTransaction = idpayTransaction;

        return trEntity;
    }


    public Uni<Void> cancelTransaction(CommonHeader headers, String transactionId) {

        Log.debugf("TransactionsService -> cancelTransaction - Input parameters: %s, %s", headers, transactionId);

        return idpayTransactionRepository.findById(transactionId) //looking for MilTransactionID in DB
                .onFailure().transform(t -> {
                    Log.errorf(t, "[%s] TransactionsService -> cancelTransaction: Error while retrieving mil transaction [%s] from DB",
                            ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB, transactionId);
                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB), List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB_MSG)))
                            .build());
                })
                .onItem().ifNull().failWith(() -> {
                    // if no transaction is found return TRANSACTION_NOT_FOUND
                    Log.errorf("TransactionsService -> cancelTransaction: transaction [%s] not found on mil DB", transactionId);

                    return new NotFoundException(Response
                            .status(Response.Status.NOT_FOUND)
                            .entity(new Errors(List.of(ErrorCode.ERROR_TRANSACTION_NOT_FOUND_MIL_DB), List.of(ErrorCode.ERROR_TRANSACTION_NOT_FOUND_MIL_DB_MSG)))
                            .build());
                }).chain(entity -> { //Transaction found
                    Log.debugf("TransactionsService -> cancelTransaction: found idpay transaction [%s] for mil transaction [%s]", entity.idpayTransaction.getIdpayTransactionId(), transactionId);

                    //call idpay to cancel transaction
                    return idpayTransactionsRestClient.deleteTransaction(entity.idpayTransaction.getIdpayMerchantId(), headers.getAcquirerId(), entity.idpayTransaction.getIdpayTransactionId())
                            .onFailure().transform(t -> {
                                Log.errorf(t, "TransactionsService -> cancelTransaction: idpay error response for idpay transaction [%s] for mil transaction [%s]", entity.idpayTransaction.getIdpayTransactionId(), transactionId);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(List.of(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES), List.of(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES_MSG)))
                                        .build());
                            })
                            .chain(() -> {
                                Log.debug("TransactionsService -> cancelTransaction: idpay getStatusTransaction service returned a 200 status");

                                IdpayTransactionEntity updEntity = updateCancelIdpayTransactionEntity(entity);

                                return idpayTransactionRepository.update(updEntity) //updating transaction in DB mil
                                        .onFailure().recoverWithItem(err -> {
                                            Log.errorf(err, "TransactionsService -> cancelTransaction: Error while updating transaction %s on db", entity.transactionId);

                                            return updEntity;
                                        })
                                        .chain(() -> Uni.createFrom().voidItem());
                                    });
                });
    }

    protected IdpayTransactionEntity updateCancelIdpayTransactionEntity(IdpayTransactionEntity entity) {
        entity.idpayTransaction.setStatus(TransactionStatus.REJECTED);
        entity.idpayTransaction.setLastUpdate(lastUpdateFormat.format(new Date()));

        return entity;
    }

    public String getIdpayMerchantId(String merchantId, String acquirerId) {
        String idpayMerchantId = merchantId;
        Log.debugf("TransactionsService -> getIdpayMerchantId: idpayMerchantId [%s] retrieved for merchantId [%s] and acquirerId [%s]", idpayMerchantId, merchantId, acquirerId);
        return idpayMerchantId;
    }

    private Transaction createTransactionFromIdpayTransactionEntity(IdpayTransactionEntity entity, byte[] secondFactor) {

        Transaction transaction = new Transaction();

        transaction.setIdpayTransactionId(entity.idpayTransaction.getIdpayTransactionId());
        transaction.setMilTransactionId(entity.idpayTransaction.getMilTransactionId());
        transaction.setInitiativeId(entity.idpayTransaction.getInitiativeId());
        transaction.setTimestamp(entity.idpayTransaction.getTimestamp());
        transaction.setGoodsCost(entity.idpayTransaction.getGoodsCost());
        transaction.setTrxCode(entity.idpayTransaction.getTrxCode());
        transaction.setCoveredAmount(entity.idpayTransaction.getCoveredAmount());
        transaction.setSecondFactor(secondFactor);

        transaction.setStatus(entity.idpayTransaction.getStatus());

        return transaction;
    }
}
