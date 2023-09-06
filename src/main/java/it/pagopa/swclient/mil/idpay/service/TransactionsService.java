package it.pagopa.swclient.mil.idpay.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.bean.Errors;
import it.pagopa.swclient.mil.idpay.ErrorCode;
import it.pagopa.swclient.mil.idpay.bean.*;
import it.pagopa.swclient.mil.idpay.client.AzureADRestClient;
import it.pagopa.swclient.mil.idpay.client.IdpayTransactionsRestClient;
import it.pagopa.swclient.mil.idpay.client.IpzsVerifyCieRestClient;
import it.pagopa.swclient.mil.idpay.client.bean.SyncTrxStatus;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionCreationRequest;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionResponse;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.IpzsVerifyCieRequest;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.Outcome;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransaction;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionEntity;
import it.pagopa.swclient.mil.idpay.dao.IdpayTransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

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

    @RestClient
    IpzsVerifyCieRestClient ipzsVerifyCieRestClient;

    @RestClient
    AzureADRestClient azureADRestClient;

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
                            }).map(ent -> createTransactionFromIdpayTransactionEntity(ent, null));
                });
    }

    protected IdpayTransactionEntity createIdpayTransactionEntity(CommonHeader headers, CreateTransaction createTransaction, TransactionCreationRequest req, TransactionResponse res) {

        IdpayTransaction idpayTransaction = new IdpayTransaction();

        idpayTransaction.setAcquirerId(res.getAcquirerId());
        idpayTransaction.setChannel(headers.getChannel());
        idpayTransaction.setMerchantId(res.getMerchantId());
        idpayTransaction.setTerminalId(headers.getTerminalId());
        idpayTransaction.setIdpayTransactionId(res.getId());

        idpayTransaction.setMilTransactionId(req.getIdTrxAcquirer());

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

        entity.transactionId = req.getIdTrxAcquirer();

        entity.idpayTransaction = idpayTransaction;

        return entity;
    }

    protected Transaction createTransactionFromIdpayTransactionEntity(IdpayTransactionEntity entity, String secondFactor) {

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
        transaction.setSecondFactor(secondFactor);

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
                    return idpayTransactionsRestClient.getStatusTransaction(headers.getMerchantId(), headers.getAcquirerId(), entity.idpayTransaction.getIdpayTransactionId())
                            .onFailure().transform(t -> {
                                Log.errorf(t, "TransactionsService -> getTransaction: idpay error response for idpay transaction [%s] for mil transaction [%s]", entity.idpayTransaction.getIdpayTransactionId(), transactionId);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(List.of(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES), List.of(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES_MSG)))
                                        .build());
                            }).chain(res -> { //response ok
                                Log.debugf("TransactionsService -> getTransaction: idpay getStatusTransaction service returned a 200 status, response: [%s]", res);

                                IdpayTransactionEntity updEntity = updateIdpayTransactionEntity(entity, res);

                                //update ok, invio la risposta al client
                                return idpayTransactionRepository.update(updEntity) //updating transaction in DB mil
                                        .onFailure().recoverWithItem(err-> {
                                            Log.errorf(err, "TransactionsService -> getTransaction: Error while updating transaction %s on db", entity.transactionId);

                                            return updEntity;
                                        }).map(ent -> createTransactionFromIdpayTransactionEntity(ent, res.getSecondFactor()));//update ok, invio la risposta al client
                            });
                });
    }

    protected IdpayTransactionEntity updateIdpayTransactionEntity(IdpayTransactionEntity entity, SyncTrxStatus res) {
        entity.idpayTransaction.setAcquirerId(res.getAcquirerId());
        entity.idpayTransaction.setMerchantId(res.getMerchantId());
        entity.idpayTransaction.setIdpayTransactionId(res.getId());
        entity.idpayTransaction.setInitiativeId(res.getInitiativeId());
        entity.idpayTransaction.setTrxCode(res.getTrxCode());
        entity.idpayTransaction.setStatus(res.getStatus());
        entity.idpayTransaction.setLastUpdate(lastUpdateFormat.format(new Date()));

        return entity;
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
                    return idpayTransactionsRestClient.deleteTransaction(headers.getMerchantId(), headers.getAcquirerId(), entity.idpayTransaction.getIdpayTransactionId())
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

    /*
    public Uni<PublicKey> verifyCie(CommonHeader headers, String transactionId, VerifyCie verifyCie) {

        Log.debugf("TransactionsService -> verifyCie - Input parameters: %s, %s, %s", headers, transactionId, verifyCie);

        return idpayTransactionRepository.findById(transactionId) //looking for MilTransactionID in DB
                .onFailure().transform(t -> {
                    Log.errorf(t, "[%s] TransactionsService -> verifyCie: Error while retrieving mil transaction [%s] from DB",
                            ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB, transactionId);
                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB), List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB_MSG)))
                            .build());
                })
                .onItem().ifNull().failWith(() -> {
                    // if no transaction is found return TRANSACTION_NOT_FOUND
                    Log.errorf("TransactionsService -> verifyCie: transaction [%s] not found on mil DB", transactionId);

                    return new NotFoundException(Response
                            .status(Response.Status.NOT_FOUND)
                            .entity(new Errors(List.of(ErrorCode.ERROR_TRANSACTION_NOT_FOUND_MIL_DB), List.of(ErrorCode.ERROR_TRANSACTION_NOT_FOUND_MIL_DB_MSG)))
                            .build());
                }).chain(entity -> { //Transaction found
                    Log.debugf("TransactionsService -> verifyCie: found idpay transaction [%s] for mil transaction [%s]", entity.idpayTransaction.getIdpayTransactionId(), transactionId);

                    if (!TransactionStatus.CREATED.equals(entity.idpayTransaction.getStatus())) {
                        throw new BadRequestException(Response
                                .status(Response.Status.BAD_REQUEST)
                                .entity(new Errors(List.of(ErrorCode.ERROR_WRONG_TRANSACTION_STATUS_MIL_DB), List.of(ErrorCode.ERROR_WRONG_TRANSACTION_STATUS_MIL_DB_MSG)))
                                .build());
                    } else {
                        //call ipzs to retrieve CIE state
                        return ipzsVerifyCieRestClient.identitycards(entity.idpayTransaction.getIdpayTransactionId(), this.createIpzsVerifyCieRequest(verifyCie, entity.idpayTransaction.getTrxCode()))
                                .onFailure().transform(t -> {
                                    if (t instanceof ClientWebApplicationException webEx && webEx.getResponse().getStatus() == 404) {//IPZS respond NOT FOUND - trasforming in BAD_REQUEST
                                        Log.errorf(t, " TransactionsService -> verifyCie: IPZS NOT FOUND for mil transaction [%s]", transactionId);

                                        return new NotFoundException(Response
                                                .status(Response.Status.BAD_REQUEST)
                                                .entity(new Errors(List.of(ErrorCode.ERROR_NOT_FOUND_IPZS_REST_SERVICES), List.of(ErrorCode.ERROR_NOT_FOUND_IPZS_REST_SERVICES_MSG)))
                                                .build());
                                    } else {
                                        Log.errorf(t, "TransactionsService -> verifyCie: IPZS error response for mil transaction [%s]", transactionId);

                                        return new InternalServerErrorException(Response
                                                .status(Response.Status.INTERNAL_SERVER_ERROR)
                                                .entity(new Errors(List.of(ErrorCode.ERROR_CALLING_IPZS_REST_SERVICES), List.of(ErrorCode.ERROR_CALLING_IPZS_REST_SERVICES_MSG)))
                                                .build());
                                    }
                                }).chain(res -> {//response ok
                                    Log.debugf("TransactionsService -> verifyCie: IPZS identitycards service returned a 200 status, response: [%s]", res);

                                    if (!Outcome.OK.equals(res.getOutcome())) {//if ipzs answers with HTTP 200 and outcome = LOST, STOLEN or EXPIRED, return HTTP 400 (bad request) with error body
                                        throw new BadRequestException(Response
                                                .status(Response.Status.BAD_REQUEST)
                                                .entity(this.decodeIpzsOutcome(res.getOutcome()))
                                                .build());
                                    } else {

                                    }
                                });
                            }
                });
    }*/

    protected IpzsVerifyCieRequest createIpzsVerifyCieRequest(VerifyCie verifyCie, String trxCode) {
        IpzsVerifyCieRequest req = new IpzsVerifyCieRequest();
        req.setNis(verifyCie.getNis());
        req.setSod(verifyCie.getSod());
        req.setKpubint(verifyCie.getCiePublicKey());
        req.setChallenge(trxCode);
        req.setChallengeSignature(verifyCie.getSignature());
        return req;
    }

    private Errors decodeIpzsOutcome(Outcome outcome) {
        return (
            switch (outcome) {
                case LOST -> new Errors(List.of(ErrorCode.ERROR_LOST_IPZS_REST_SERVICES), List.of(ErrorCode.ERROR_LOST_IPZS_REST_SERVICES_MSG));
                case STOLEN -> new Errors(List.of(ErrorCode.ERROR_STOLEN_IPZS_REST_SERVICES), List.of(ErrorCode.ERROR_STOLEN_IPZS_REST_SERVICES_MSG));
                case EXPIRED -> new Errors(List.of(ErrorCode.ERROR_EXPIRED_IPZS_REST_SERVICES), List.of(ErrorCode.ERROR_EXPIRED_IPZS_REST_SERVICES_MSG));
                default -> new Errors(List.of(ErrorCode.ERROR_UNKNOWN_IPZS_REST_SERVICES), List.of(ErrorCode.ERROR_UNKNOWN_IPZS_REST_SERVICES_MSG));
            }
        );
    }
}
