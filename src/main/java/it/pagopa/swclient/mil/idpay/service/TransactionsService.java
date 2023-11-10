package it.pagopa.swclient.mil.idpay.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.bean.Errors;
import it.pagopa.swclient.mil.idpay.ErrorCode;
import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.UnwrapKeyRequest;
import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.UnwrapKeyResponse;
import it.pagopa.swclient.mil.idpay.azurekeyvault.client.AzureKeyVaultClient;
import it.pagopa.swclient.mil.idpay.azurekeyvault.service.AzureKeyVaultService;
import it.pagopa.swclient.mil.idpay.azurekeyvault.util.EncryptUtil;
import it.pagopa.swclient.mil.idpay.bean.*;
import it.pagopa.swclient.mil.idpay.bean.cer.CertificateBundle;
import it.pagopa.swclient.mil.idpay.client.AzureADRestClient;
import it.pagopa.swclient.mil.idpay.client.IdpayAuthorizeTransactionRestClient;
import it.pagopa.swclient.mil.idpay.client.IdpayTransactionsRestClient;
import it.pagopa.swclient.mil.idpay.client.IpzsVerifyCieRestClient;
import it.pagopa.swclient.mil.idpay.client.bean.PreAuthPaymentResponseDTO;
import it.pagopa.swclient.mil.idpay.client.bean.SyncTrxStatus;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionCreationRequest;
import it.pagopa.swclient.mil.idpay.client.bean.TransactionResponse;
import it.pagopa.swclient.mil.idpay.client.bean.azure.AccessToken;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.IpzsVerifyCieRequest;
import it.pagopa.swclient.mil.idpay.client.bean.ipzs.IpzsVerifyCieResponse;
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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransactionsService {

    private final SimpleDateFormat lastUpdateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Inject
    IdpayTransactionRepository idpayTransactionRepository;

    @Inject
    EncryptUtil encryptUtil;

    @Inject
    AzureKeyVaultService azureKeyVaultService;

    @RestClient
    IdpayTransactionsRestClient idpayTransactionsRestClient;

    @RestClient
    IpzsVerifyCieRestClient ipzsVerifyCieRestClient;

    @RestClient
    AzureADRestClient azureADRestClient;

    @RestClient
    AzureKeyVaultClient azureKeyVaultClient;

    @RestClient
    IdpayAuthorizeTransactionRestClient idpayAuthorizeTransactionRestClient;

    private static final String BEARER = "Bearer ";

    public static final String VAULT = "https://vault.azure.net";

    @ConfigProperty(name = "idpay.transactions.days-before", defaultValue = "30")
    int getTransactionsDaysBefore;

    @ConfigProperty(name = "idpay.transactions.max-transactions", defaultValue = "30")
    int getTransactionsMaxTransactions;

    @ConfigProperty(name = "azure-auth-api.identity")
    String identity;

    @ConfigProperty(name = "azure-cert.name")
    String certName;

    public Uni<Transaction> createTransaction(CommonHeader headers, CreateTransaction createTransaction) {

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
                            .onFailure().transform(err -> {
                                Log.errorf(err, "TransactionsService -> createTransaction: Error while storing transaction %s on db", entity.transactionId);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(List.of(ErrorCode.ERROR_STORING_DATA_IN_DB), List.of(ErrorCode.ERROR_STORING_DATA_IN_DB_MSG)))
                                        .build());
                            }).map(ent -> createTransactionFromIdpayTransactionEntity(ent, null, res.getTrxTxtUrl(), true));
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

    protected Transaction createTransactionFromIdpayTransactionEntity(IdpayTransactionEntity entity, String secondFactor, String qrCode, boolean createTransaction) {

        Transaction transaction = new Transaction();

        transaction.setIdpayTransactionId(entity.idpayTransaction.getIdpayTransactionId());
        transaction.setMilTransactionId(entity.idpayTransaction.getMilTransactionId());
        transaction.setInitiativeId(entity.idpayTransaction.getInitiativeId());
        transaction.setTimestamp(entity.idpayTransaction.getTimestamp());
        transaction.setGoodsCost(entity.idpayTransaction.getGoodsCost());
        if (createTransaction) {
            transaction.setChallenge(entity.idpayTransaction.getTrxCode().getBytes(StandardCharsets.UTF_8));
        }
        transaction.setTrxCode(entity.idpayTransaction.getTrxCode());
        transaction.setQrCode(qrCode);
        transaction.setCoveredAmount(entity.idpayTransaction.getCoveredAmount());
        transaction.setStatus(entity.idpayTransaction.getStatus());
        transaction.setSecondFactor(secondFactor);

        return transaction;
    }

    public Uni<Transaction> getTransaction(CommonHeader headers, String transactionId) {

        Log.debugf("TransactionsService -> getTransaction - Input parameters: %s, %s", headers, transactionId);

        return getIdpayTransactionEntity(transactionId) //looking for MilTransactionID in DB
            .chain(entity -> { //Transaction found
                Log.debugf("TransactionsService -> getTransaction: found idpay transaction [%s] for mil transaction [%s]", entity.idpayTransaction.getIdpayTransactionId(), transactionId);

                    //call idpay to retrieve current state
                    return this.getStatusTransaction(entity.idpayTransaction.getIdpayMerchantId(), headers.getAcquirerId(), entity.idpayTransaction.getIdpayTransactionId())
                            .chain(res -> { //response ok
                            Log.debugf("TransactionsService -> getTransaction: idpay getStatusTransaction service returned a 200 status, response: [%s]", res);

                                IdpayTransactionEntity updEntity = updateIdpayTransactionEntity(headers, entity, res);

                                if (!updEntity.idpayTransaction.equals(entity.idpayTransaction)) {
                                    Log.debugf("TransactionsService -> getTransaction: transaction situation is changed, make an update");
                                    return idpayTransactionRepository.update(updEntity) //updating transaction in DB mil
                                            .onFailure().recoverWithItem(err -> {
                                                Log.errorf(err, "TransactionsService -> cancelTransaction: Error while updating transaction %s on db", entity.transactionId);

                                                return updEntity;
                                            }).chain(uEnt -> getSecFactAndRespond(uEnt, res));
                                } else {
                                    Log.debugf("TransactionsService -> getTransaction: transaction situation is NOT changed");
                                    return getSecFactAndRespond(updEntity, res);
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
        idpayTransaction.setByCie(entity.idpayTransaction.getByCie());

        IdpayTransactionEntity trEntity = new IdpayTransactionEntity();

        trEntity.transactionId = entity.transactionId;

        trEntity.idpayTransaction = idpayTransaction;

        return trEntity;
    }


    public Uni<Void> cancelTransaction(CommonHeader headers, String transactionId) {

        Log.debugf("TransactionsService -> cancelTransaction - Input parameters: %s, %s", headers, transactionId);

        return getIdpayTransactionEntity(transactionId) //looking for MilTransactionID in DB
                .chain(entity -> { //Transaction found
                    Log.debugf("TransactionsService -> cancelTransaction: found idpay transaction [%s] for mil transaction [%s]", entity.idpayTransaction.getIdpayTransactionId(), transactionId);

                    //call idpay to retrieve current state
                    return this.getStatusTransaction(entity.idpayTransaction.getIdpayMerchantId(), headers.getAcquirerId(), entity.idpayTransaction.getIdpayTransactionId())
                            .chain(status -> //response ok
                                //call idpay to cancel transaction
                                idpayTransactionsRestClient.deleteTransaction(entity.idpayTransaction.getIdpayMerchantId(), headers.getAcquirerId(), entity.idpayTransaction.getIdpayTransactionId())
                                        .onFailure().transform(t -> {
                                            if (t instanceof ClientWebApplicationException webEx && webEx.getResponse().getStatus() == 404) {
                                                Log.errorf(t, " TransactionsService -> cancelTransaction: idpay NOT FOUND for idpay transaction [%s] for mil transaction [%s]", entity.idpayTransaction.getIdpayTransactionId(), transactionId);
                                                Errors errors = new Errors(List.of(ErrorCode.ERROR_NOT_FOUND_IDPAY_REST_SERVICES), List.of(ErrorCode.ERROR_NOT_FOUND_IDPAY_REST_SERVICES_MSG));
                                                return new NotFoundException(Response
                                                        .status(Response.Status.NOT_FOUND)
                                                        .entity(errors)
                                                        .build());
                                            } else {
                                                Log.errorf(t, "TransactionsService -> cancelTransaction: idpay error response for idpay transaction [%s] for mil transaction [%s]", entity.idpayTransaction.getIdpayTransactionId(), transactionId);

                                                return new InternalServerErrorException(Response
                                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(new Errors(List.of(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES), List.of(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES_MSG)))
                                                        .build());
                                            }
                                        })
                                        .chain(() -> {
                                            Log.debug("TransactionsService -> cancelTransaction: idpay getStatusTransaction service returned a 200 status");

                                            IdpayTransactionEntity updEntity = updateIdpayTransactionEntity(entity, TransactionStatus.AUTHORIZED.equals(status.getStatus()) ? TransactionStatus.CANCELLED : TransactionStatus.ABORTED);

                                            return idpayTransactionRepository.update(updEntity) //updating transaction in DB mil
                                                    .onFailure().recoverWithItem(err -> {
                                                        Log.errorf(err, "TransactionsService -> cancelTransaction: Error while updating transaction %s on db", entity.transactionId);

                                                        return updEntity;
                                                    })
                                                    .chain(() -> Uni.createFrom().voidItem());
                                        })
                            );
            });
    }

    protected IdpayTransactionEntity updateIdpayTransactionEntity(IdpayTransactionEntity entity, TransactionStatus status) {
        entity.idpayTransaction.setStatus(status);
        entity.idpayTransaction.setLastUpdate(lastUpdateFormat.format(new Date()));

        return entity;
    }


    public Uni<PublicKeyIDPay> verifyCie(CommonHeader headers, String transactionId, VerifyCie verifyCie) {

        Log.debugf("TransactionsService -> verifyCie - Input parameters: %s, %s, %s", headers, transactionId, verifyCie);

        return getIdpayTransactionEntity(transactionId) //looking for MilTransactionID in DB
            .chain(entity -> { //Transaction found
                Log.debugf("TransactionsService -> verifyCie: found idpay transaction [%s] for mil transaction [%s]", entity.idpayTransaction.getIdpayTransactionId(), transactionId);

                return this.updateByCie(entity)
                        .chain(ent -> this.callIpzs(ent, verifyCie, transactionId));
                }).chain(Unchecked.function(res -> {//response ok
                    Log.debugf("TransactionsService -> verifyCie: IPZS identitycards service returned a 200 status, response: [%s]", res);

                    if (!Outcome.OK.equals(res.getOutcome())) {//if ipzs answers with HTTP 200 and outcome = LOST, STOLEN or EXPIRED, return HTTP 400 (bad request) with error body
                        throw new BadRequestException(Response
                                .status(Response.Status.BAD_REQUEST)
                                .entity(this.decodeIpzsOutcome(res.getOutcome()))
                                .build());
                    } else {

                        return azureADRestClient.getAccessToken(identity, VAULT)
                                .onFailure().transform(t -> {
                                    Log.errorf(t, "TransactionsService -> verifyCie: Azure AD error response for mil transaction [%s]", transactionId);

                                    return new InternalServerErrorException(Response
                                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity(new Errors(List.of(ErrorCode.ERROR_CALLING_AZUREAD_REST_SERVICES), List.of(ErrorCode.ERROR_CALLING_AZUREAD_REST_SERVICES_MSG)))
                                            .build());
                                }).chain(token -> {
                                    Log.debugf("TransactionsService -> verifyCie:  Azure AD service returned a 200 status, response: [%s]", token);

                                    return azureKeyVaultService.getAzureKVKey(token, headers)
                                            .onFailure().transform(t -> {
                                                Log.errorf(t, "TransactionsService -> verifyCie: Azure Key Vault error for mil transaction [%s]", transactionId);

                                                return new InternalServerErrorException(Response
                                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_KEY_PAIR), List.of(ErrorCode.ERROR_RETRIEVING_KEY_PAIR_MSG)))
                                                        .build());
                                            });
                                });
                    }
                }));
    }

    protected IpzsVerifyCieRequest createIpzsVerifyCieRequest(VerifyCie verifyCie, String trxCode) {
        IpzsVerifyCieRequest req = new IpzsVerifyCieRequest();
        req.setNis(verifyCie.getNis());
        req.setSod(verifyCie.getSod());
        req.setKpubint(verifyCie.getCiePublicKey());
        req.setChallenge(trxCode.getBytes(StandardCharsets.UTF_8));
        req.setChallengeSignature(verifyCie.getSignature());
        return req;
    }

    private Errors decodeIpzsOutcome(Outcome outcome) {
        if (Outcome.LOST.equals(outcome))
            return new Errors(List.of(ErrorCode.ERROR_LOST_IPZS_REST_SERVICES), List.of(ErrorCode.ERROR_LOST_IPZS_REST_SERVICES_MSG));
        else if (Outcome.STOLEN.equals(outcome))
            return new Errors(List.of(ErrorCode.ERROR_STOLEN_IPZS_REST_SERVICES), List.of(ErrorCode.ERROR_STOLEN_IPZS_REST_SERVICES_MSG));
        else
            return new Errors(List.of(ErrorCode.ERROR_EXPIRED_IPZS_REST_SERVICES), List.of(ErrorCode.ERROR_EXPIRED_IPZS_REST_SERVICES_MSG));
    }

    private Uni<IpzsVerifyCieResponse> callIpzs(IdpayTransactionEntity entity, VerifyCie verifyCie, String transactionId) {
        if (!TransactionStatus.CREATED.equals(entity.idpayTransaction.getStatus())) {
            Log.warnf(" TransactionsService -> verifyCie: Transacton status [%s] not allowed for mil transaction [%s]", entity.idpayTransaction.getStatus(), transactionId);
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

                            return new BadRequestException(Response
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
                    });
        }
    }

    public Uni<Response> authorizeTransaction(CommonHeader headers, AuthorizeTransaction authorizeTransaction, String milTransactionId) {
        Log.debugf("TransactionsService -> authorizeTransaction - Input parameters: %s, %s, %s", headers, milTransactionId, authorizeTransaction);

        return getIdpayTransactionEntity(milTransactionId) //looking for MilTransactionID in DB
                .chain(Unchecked.function(dbData -> {

                    // Transaction found
                    Log.debugf("TransactionsService -> authorizeTransaction: found idpay transaction [%s] for mil transaction [%s]", dbData.idpayTransaction.getIdpayTransactionId(), milTransactionId);

                    return transactionIdentified(dbData, milTransactionId)
                            .chain(accessToken -> {

                                // If retrieve access token success, start unwrapping key
                                Log.debugf("TransactionsService -> authorizeTransaction: Azure AD service returned a 200 status, response: [%s]", accessToken);

                                return unwrapKey(authorizeTransaction, accessToken)
                                        .chain(unwrappedKey -> {

                                            // Unwrap key success, start retrieving id pay public key
                                            Log.debugf("TransactionsService -> authorizeTransaction: Azure KV unwrapping service returned a 200 status, response: [%s]", unwrappedKey);

                                            return retrieveIdpayPublicKey(dbData.idpayTransaction.getAcquirerId())
                                                    .chain(Unchecked.function(publicKeyIDPay -> {

                                                        // If idpay public key retrieval success, starth authorize transaction
                                                        Log.debugf("TransactionsService -> authorizeTransaction: IDPay returned a 200 status, response with public key: [%s]", publicKeyIDPay);

                                                        try {

                                                            // Start trying to encrypt session key with public key retrieved
                                                            String encryptedSessionKey = encryptUtil.encryptSessionKeyForIdpay(publicKeyIDPay, unwrappedKey.getValue());

                                                            PinBlockDTO pinBlock = PinBlockDTO.builder()
                                                                    .encryptedPinBlock(authorizeTransaction.getAuthCodeBlockData().getAuthCodeBlock())
                                                                    .encryptedKey(encryptedSessionKey)
                                                                    .build();

                                                            return authorize(dbData, pinBlock);
                                                        } catch (NoSuchAlgorithmException | InvalidKeySpecException |
                                                                 NoSuchPaddingException | InvalidKeyException |
                                                                 IllegalBlockSizeException |
                                                                 BadPaddingException error) {

                                                            // If encrypt retrieve some error, return INTERNAL_SERVER_ERROR
                                                            Log.errorf("Error during encrypting session key using IDPay public key [%s]", publicKeyIDPay);

                                                            throw new InternalServerErrorException(Response
                                                                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                                                                    .entity(new Errors(List.of(ErrorCode.ERROR_ENCRYPTING_SESSION_KEY), List.of(ErrorCode.ERROR_ENCRYPTING_SESSION_KEY_MSG)))
                                                                    .build());
                                                        }
                                                    }));
                                        });
                            });
                }));
    }

    private Uni<AccessToken> transactionIdentified(IdpayTransactionEntity dbData, String milTransactionId) {
        if (!dbData.idpayTransaction.getStatus().equals(TransactionStatus.IDENTIFIED)) {
            // If transaction is NOT IDENTIFIED, return BAD_REQUEST
            Log.errorf("TransactionsService -> authorizeTransaction: idpay transaction with id [%s] is NOT IDENTIFIED", dbData.idpayTransaction.getIdpayTransactionId());

            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Errors(List.of(ErrorCode.ERROR_WRONG_TRANSACTION_STATUS_MIL_DB), List.of(ErrorCode.ERROR_WRONG_TRANSACTION_STATUS_MIL_DB_MSG)))
                    .build());
        } else {
            // If transaction is IDENTIFIED, start retrieving azure access token

            return azureADRestClient.getAccessToken(identity, VAULT)
                    .onFailure().transform(Unchecked.function(t -> {

                        // If retrieve access token fails, return INTERNAL_SERVER_ERROR
                        Log.errorf(t, "TransactionsService -> authorizeTransaction: Azure AD error response for mil transaction [%s]", milTransactionId);

                        throw new InternalServerErrorException(Response
                                .status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(new Errors(List.of(ErrorCode.ERROR_CALLING_AZUREAD_REST_SERVICES), List.of(ErrorCode.ERROR_CALLING_AZUREAD_REST_SERVICES_MSG)))
                                .build());
                    }))
                    .chain(token -> Uni.createFrom().item(token));
        }
    }

    private Uni<UnwrapKeyResponse> unwrapKey(AuthorizeTransaction authorizeTransaction, AccessToken token) {
        UnwrapKeyRequest unwrapKeyRequest = UnwrapKeyRequest
                .builder()
                .alg("RSA-OAEP-256")
                .value(authorizeTransaction.getAuthCodeBlockData().getEncSessionKey())
                .build();

        return azureKeyVaultClient.unwrapKey(BEARER + token.getToken(), authorizeTransaction.getAuthCodeBlockData().getKid(), unwrapKeyRequest)
                .onFailure().transform(Unchecked.function(t -> {

                    // If unwrap key fails, return INTERNAL_SERVER_ERROR
                    Log.errorf(t, "TransactionsService -> authorizeTransaction: Azure KV error response while unwrapping session key [%s]", authorizeTransaction.getAuthCodeBlockData().getEncSessionKey());

                    throw new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_KEY_PAIR), List.of(ErrorCode.ERROR_RETRIEVING_KEY_PAIR_MSG)))
                            .build());
                })).chain(unwrappedKey -> Uni.createFrom().item(unwrappedKey));
    }

    private Uni<PublicKeyIDPay> retrieveIdpayPublicKey(String acquirerId) {
        return idpayAuthorizeTransactionRestClient.retrieveIdpayPublicKey(acquirerId)
                .onFailure().transform(Unchecked.function(t -> {

                    // If idpay public key retrieval fails, return INTERNAL_SERVER_ERROR
                    Log.errorf(t, "TransactionsService -> authorizeTransaction: IDPay error response while retrieving public key.");

                    throw new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_PUBLIC_KEY_IDPAY), List.of(ErrorCode.ERROR_RETRIEVING_PUBLIC_KEY_IDPAY_MSG)))
                            .build());
                })).chain(publicKeyIDPay -> Uni.createFrom().item(publicKeyIDPay));
    }

    private Uni<Response> authorize(IdpayTransactionEntity dbData, PinBlockDTO pinBlock) {
            return idpayAuthorizeTransactionRestClient.authorize(dbData.idpayTransaction.getIdpayMerchantId(), dbData.idpayTransaction.getAcquirerId(), dbData.idpayTransaction.getIdpayTransactionId(), pinBlock)
                    .onFailure().transform(Unchecked.function(t -> {

                    // Error 500 while trying to authorize transaction
                    Log.errorf(t, "TransactionsService -> authorizeTransaction: error response while authorizing transaction.");
                    Errors errors = new Errors(List.of(ErrorCode.ERROR_CALLING_AUTHORIZE_REST_SERVICES), List.of(ErrorCode.ERROR_CALLING_AUTHORIZE_REST_SERVICES_MSG));

                    throw new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(errors)
                            .build());
                }))
                .chain(finalResult -> {
                    if (finalResult.getAuthTransactionResponseOk() != null) {

                        // If all went ok, send 200 OK to client
                        Log.debugf("TransactionsService -> authorizeTransaction: call to authorize returned a 200 status, response with public key: [%s]", finalResult.getAuthTransactionResponseOk());

                        // Start updating transaction with new status AUTHORIZED
                        return updateAuthorizeTransactionStatus(dbData)
                                .onItem()
                                .transform(result -> result);
                    } else if (finalResult.getAuthTransactionResponseWrong() != null) {

                        // If IDPay responds with WRONG_AUTH_CODE, send BAD_REQUEST to client
                        Log.errorf("TransactionsService -> authorizeTransaction: error IDPay responds with WRONG_AUTH_CODE for transaction: [%s]", dbData.transactionId);
                        Errors errors = new Errors(List.of(ErrorCode.ERROR_IDPAY_WRONG_AUTH_CODE), List.of(ErrorCode.ERROR_IDPAY_WRONG_AUTH_CODE_MSG));

                        return Uni.createFrom().item((Response
                                .status(Response.Status.BAD_REQUEST)
                                .entity(errors)
                                .build()));
                    } else {

                        // If any other from IDPay, send INTERNAL_SERVER_ERROR to client
                        Log.errorf("TransactionsService -> authorizeTransaction: IDPay responds with unknown error 500 for transaction: [%s]", dbData.transactionId);
                        Errors errors = new Errors(List.of(ErrorCode.ERROR_IDPAY_UNKNOWN_ERROR_CODE), List.of(ErrorCode.ERROR_IDPAY_UNKNOWN_ERROR_MSG));

                        return Uni.createFrom().item((Response
                                .status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(errors)
                                .build()));
                    }
                });
    }

    private Uni<Response> updateAuthorizeTransactionStatus(IdpayTransactionEntity dbData) {
        Log.debugf("TransactionsService -> authorizeTransaction: transaction status is changed, make an update");

        IdpayTransactionEntity updEntity = updateIdpayTransactionEntity(dbData, TransactionStatus.AUTHORIZED);

        return idpayTransactionRepository.update(updEntity)
                .onFailure().transform(Unchecked.function(err -> {
                    Log.errorf(err, "TransactionsService -> authorizeTransaction: Error while updating transaction %s on db", dbData.transactionId);

                    throw new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_STORING_DATA_IN_DB), List.of(ErrorCode.ERROR_STORING_DATA_IN_DB_MSG)))
                            .build());
                }))
                .onItem().transform(transactionSaved -> {
                    Log.debugf("TransactionsService -> authorizeTransaction: transaction saved: [%s]", transactionSaved);

                    return Response.status(Response.Status.OK).build();
                });
    }

    public Uni<IdpayTransactionEntity> getIdpayTransactionEntity(String transactionId) {
        return idpayTransactionRepository.findById(transactionId) //looking for MilTransactionID in DB
                .onFailure().transform(t -> {
                    Log.errorf(t, "[%s] TransactionsService -> getIdpayTransactionEntity: Error while retrieving mil transaction [%s] from DB",
                            ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB, transactionId);
                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB), List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB_MSG)))
                            .build());
                })
                .onItem().ifNull().failWith(() -> {
                    // if no transaction is found return TRANSACTION_NOT_FOUND
                    Log.errorf("TransactionsService -> getIdpayTransactionEntity: transaction [%s] not found on mil DB", transactionId);

                    return new NotFoundException(Response
                            .status(Response.Status.NOT_FOUND)
                            .entity(new Errors(List.of(ErrorCode.ERROR_TRANSACTION_NOT_FOUND_MIL_DB), List.of(ErrorCode.ERROR_TRANSACTION_NOT_FOUND_MIL_DB_MSG)))
                            .build());
                });
    }

    public String getIdpayMerchantId(String merchantId, String acquirerId) {
        Log.debugf("TransactionsService -> getIdpayMerchantId: idpayMerchantId [%s] retrieved for merchantId [%s] and acquirerId [%s]", merchantId, merchantId, acquirerId);
        return merchantId;
    }

    public Uni<GetTransactionsResponse> getLastTransactions(CommonHeader headers) {

        Log.debugf("TransactionsService -> getLastTransactions - Input parameters: %s", headers);

        return idpayTransactionRepository.find(
                        """
                                idpayTransaction.status in ?1 and
                                idpayTransaction.terminalId = ?2 and
                                idpayTransaction.merchantId = ?3 and
                                idpayTransaction.channel    = ?4 and
                                idpayTransaction.acquirerId = ?5 and
                                idpayTransaction.timestamp >= ?6
                              """,
                        Sort.by("idpayTransaction.timestamp").descending(),
                        List.of(TransactionStatus.CANCELLED, TransactionStatus.AUTHORIZED),
                        headers.getTerminalId(),
                        headers.getMerchantId(),
                        headers.getChannel(),
                        headers.getAcquirerId(),
                        LocalDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC)
                                .toLocalDate().atTime(LocalTime.MIN) // set midnight
                                .minus(getTransactionsDaysBefore, ChronoUnit.DAYS) // transaction of the last 30 days
                                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .withBatchSize(getTransactionsMaxTransactions)
                .page(Page.ofSize(getTransactionsMaxTransactions))
                .list()
                .onFailure().transform(t -> {
                    Log.errorf(t, "[%s] TransactionsService -> getLastTransactions: Error while retrieving transactions from DB",
                            ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB);
                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB), List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB_MSG)))
                            .build());
                })
                .map(txEntityList -> {
                    var transactionList = txEntityList.stream().map(txEntity ->
                            Transaction.builder()
                                    .idpayTransactionId(txEntity.idpayTransaction.getIdpayTransactionId())
                                    .milTransactionId(txEntity.idpayTransaction.getMilTransactionId())
                                    .initiativeId(txEntity.idpayTransaction.getInitiativeId())
                                    .timestamp(txEntity.idpayTransaction.getTimestamp())
                                    .goodsCost(txEntity.idpayTransaction.getGoodsCost())
                                    .trxCode(txEntity.idpayTransaction.getTrxCode())
                                    .coveredAmount(txEntity.idpayTransaction.getCoveredAmount())
                                    .status(txEntity.idpayTransaction.getStatus())
                                    .lastUpdate(txEntity.idpayTransaction.getLastUpdate())
                                    .build()).toList();
                    GetTransactionsResponse getTransactionsResponse = new GetTransactionsResponse();
                    getTransactionsResponse.setTransactions(transactionList);

                    return getTransactionsResponse;
                });
    }

    private Uni<PreAuthPaymentResponseDTO> getSecondFactor(String idpayMerchantId, String xAcquirerId, String transactionId) {

        return idpayTransactionsRestClient.putPreviewPreAuthPayment(idpayMerchantId, xAcquirerId, transactionId)
                .onFailure().transform(t -> {
                    Log.errorf(t, "[%s] TransactionsService -> getSecondFactor: Error while retrieving secondFactor for idpay transaction [%s]",
                            ErrorCode.ERROR_RETRIEVING_SECOND_FACTOR, transactionId);
                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_SECOND_FACTOR), List.of(ErrorCode.ERROR_RETRIEVING_SECOND_FACTOR_MSG)))
                            .build());
                });
    }


    private Uni<IdpayTransactionEntity> updateByCie(IdpayTransactionEntity entity) {

        entity.idpayTransaction.setLastUpdate(lastUpdateFormat.format(new Date()));
        entity.idpayTransaction.setByCie(true);

        return idpayTransactionRepository.update(entity)
                .onFailure().transform(err -> new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_STORING_DATA_IN_DB), List.of(ErrorCode.ERROR_STORING_DATA_IN_DB_MSG)))
                            .build())
                );
    }

    private Uni<Transaction> getSecFactAndRespond(IdpayTransactionEntity entity, SyncTrxStatus res) {
        if (TransactionStatus.IDENTIFIED.equals(res.getStatus()) && entity.idpayTransaction.getByCie() != null && Boolean.TRUE.equals(entity.idpayTransaction.getByCie())) {
            return getSecondFactor(entity.idpayTransaction.getIdpayMerchantId(), entity.idpayTransaction.getAcquirerId(), entity.idpayTransaction.getIdpayTransactionId())
                    .map(secFactResp -> createTransactionFromIdpayTransactionEntity(entity, secFactResp.getSecondFactor(), null, false));
        } else {
            return Uni.createFrom().item(createTransactionFromIdpayTransactionEntity(entity, null, null, false));
        }
    }

    private Uni<SyncTrxStatus> getStatusTransaction(String idpayMerchantId, String xAcquirerId, String transactionId) {
        return idpayTransactionsRestClient.getStatusTransaction(idpayMerchantId, xAcquirerId, transactionId)
                .onFailure().transform(t -> {
                    if (t instanceof ClientWebApplicationException webEx && webEx.getResponse().getStatus() == 404) {
                        Log.errorf(t, " TransactionsService -> getStatusTransaction: idpay NOT FOUND for mil transaction [%s]", transactionId);
                        Errors errors = new Errors(List.of(ErrorCode.ERROR_NOT_FOUND_IDPAY_REST_SERVICES), List.of(ErrorCode.ERROR_NOT_FOUND_IDPAY_REST_SERVICES_MSG));
                        return new NotFoundException(Response
                                .status(Response.Status.NOT_FOUND)
                                .entity(errors)
                                .build());
                    } else {
                        Log.errorf(t, "TransactionsService -> getStatusTransaction: idpay error response for mil transaction [%s]", transactionId);

                        return new InternalServerErrorException(Response
                                .status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(new Errors(List.of(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES), List.of(ErrorCode.ERROR_CALLING_IDPAY_REST_SERVICES_MSG)))
                                .build());
                    }
                });
    }

    public Uni<CertificateBundle> getCertificate() {
        Log.debugf("TransactionsService -> getCertificate - certName: [%s]", certName);

        return azureADRestClient.getAccessToken(identity, VAULT)
                .onFailure().transform(t -> {
                    Log.errorf(t, "TransactionsService -> getCertificate: Azure AD error response for certName [%s]", certName);

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_CALLING_AZUREAD_REST_SERVICES), List.of(ErrorCode.ERROR_CALLING_AZUREAD_REST_SERVICES_MSG)))
                            .build());
                }).chain(token -> {
                    Log.debugf("TransactionsService -> getCertificate:  Azure AD service returned a 200 status, response: [%s]", token);

                    return azureKeyVaultClient.getCertificate(BEARER + token.getToken(), certName)
                            .onFailure().transform(t -> {
                                Log.errorf(t, "TransactionsService -> getCertificate: Azure Key Vault error for certName [%s]", certName);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_CERT_FOR_IDPAY), List.of(ErrorCode.ERROR_RETRIEVING_CERT_FOR_IDPAY_MSG)))
                                        .build());
                            });
                });
    }
}
