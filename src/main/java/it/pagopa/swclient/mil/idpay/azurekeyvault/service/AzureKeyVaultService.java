package it.pagopa.swclient.mil.idpay.azurekeyvault.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.bean.Errors;
import it.pagopa.swclient.mil.idpay.ErrorCode;
import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.*;
import it.pagopa.swclient.mil.idpay.azurekeyvault.client.AzureKeyVaultClient;
import it.pagopa.swclient.mil.idpay.azurekeyvault.util.KidUtil;
import it.pagopa.swclient.mil.idpay.bean.KeyOp;
import it.pagopa.swclient.mil.idpay.bean.KeyType;
import it.pagopa.swclient.mil.idpay.bean.PublicKeyIDPay;
import it.pagopa.swclient.mil.idpay.bean.PublicKeyUse;
import it.pagopa.swclient.mil.idpay.client.bean.azure.AccessToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class AzureKeyVaultService {

    @RestClient
    AzureKeyVaultClient azureKeyVaultClient;
    private static final String RSA = "RSA";
    @ConfigProperty(name = "keysize", defaultValue = "4096")
    int keysize;
    private static final String WRAPKEY = "wrapKey";
    private static final String UNWRAPKEY = "unwrapKey";
    private static final String[] OPS = new String[] {
            WRAPKEY, UNWRAPKEY
    };
    @ConfigProperty(name = "cryptoperiod", defaultValue = "86400")
    long cryptoperiod;
    private static final String PURGEABLE = "Purgeable";

    private static final String BEARER = "Bearer ";

    @Inject
    KidUtil kidUtil;

    public Uni<PublicKeyIDPay> getAzureKVKey(AccessToken accessToken, CommonHeader headers) {

        if (accessToken.getAccess_token() == null) {
            throw new InternalServerErrorException(Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Errors(List.of(ErrorCode.AZUREAD_ACCESS_TOKEN_IS_NULL), List.of(ErrorCode.AZUREAD_ACCESS_TOKEN_IS_NULL_MSG)))
                    .build());
        }

        String keyName = "idpay-wrap-key-".concat(headers.getAcquirerId()).concat("-").concat("POS".equals(headers.getChannel()) ? headers.getMerchantId().concat("-") : "").concat(headers.getTerminalId());

        Log.debugf("AzureKeyVaultService -> getAzureKVKey: call Azure Key Vault for keyName: [%s]", keyName);

        return azureKeyVaultClient.getKey(BEARER + accessToken, keyName)
                .onItemOrFailure()
                    .transformToUni((getKeyResponse, error) -> {
                        if (error != null && !(error instanceof ClientWebApplicationException webEx && webEx.getResponse().getStatus() == 404)) {
                            Log.errorf(error, "AzureKeyVaultService -> getAzureKVKey: Azure Key Vault error response for keyName [%s]", keyName);

                            throw new InternalServerErrorException(Response
                                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                                    .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_KEY_PAIR), List.of(ErrorCode.ERROR_RETRIEVING_KEY_PAIR_MSG)))
                                    .build());
                        } else if (error != null || (getKeyResponse != null && (!isKeyValid(getKeyResponse.getKey()) || !isKeyNotYetExpired(getKeyResponse.getKey())))) {//Se NOT FOUND o Expired chiamo CreateKey
                            return createAzureKVKey(accessToken, keyName);
                        } else {
                            return Uni.createFrom().item(getPublicKey(getKeyResponse.getKey()));
                        }
                    });

    }


    private Uni<PublicKeyIDPay> createAzureKVKey(AccessToken accessToken, String keyName) {

        Log.debugf("AzureKeyVaultService -> createAzureKVKey: call Azure Key Vault create key for keyName: [%s]", keyName);

        long now = Instant.now().getEpochSecond();
        KeyAttributes attributes = new KeyAttributes(now, now + cryptoperiod, now, now, true, PURGEABLE, 0, false);

        CreateKeyRequest createKeyRequest = new CreateKeyRequest(RSA, keysize, OPS, attributes);

        return azureKeyVaultClient.createKey(BEARER + accessToken, keyName, createKeyRequest)
                .onFailure().transform(t -> {
                    Log.errorf(t, "[%s] AzureKeyVaultService -> createAzureKVKey: Azure Key Vault error response for keyName [%s]", ErrorCode.ERROR_GENERATING_KEY_PAIR, keyName);
                    throw new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_GENERATING_KEY_PAIR), List.of(ErrorCode.ERROR_GENERATING_KEY_PAIR_MSG)))
                            .build());
                })
                .map(resp -> getPublicKey(resp.getKey()));
    }

    private boolean isKeyValid(KeyDetails key) {
        return isKeyValid((Key) key) && isKeyTypeRsa(key) && isKeySuitableForWrap(key);
    }

    private boolean isKeyTypeRsa(KeyDetails key) {
        if (Objects.equals(key.getKty(), RSA)) {
            Log.debugf("The key type of [%s] is RSA.", key.getKid());
            return true;
        } else {
            Log.warnf("The key type of [%s] is not RSA. Found [%s].", key.getKid(), key.getKty());
            return false;
        }
    }

    private boolean isKeySuitableForWrap(KeyDetails key) {
        String[] keyOps = key.getKeyOps();
        if (keyOps != null) {
            List<String> keyOpList = Arrays.asList(keyOps);
            if (keyOpList.contains(WRAPKEY) && keyOpList.contains(UNWRAPKEY)) {
                Log.debugf("The key [%s] is suitable for wrap.", key.getKid());
                return true;
            } else {
                Log.warnf("The key [%s] is not suitable for wrap. Found [%s].", key.getKid(), keyOpList);
                return false;
            }
        } else {
            Log.errorf("The key [%s] has null ops.", key.getKid());
            return false;
        }
    }

    private boolean isKeyValid(Key key) {
        if (key.getAttributes() == null) {
            Log.errorf("The key [%s] has null attributes.", key.getKid());
            return false;
        } else {
            return isKeyEnabled(key)
                    && isKeyCreationTimestampCoherent(key)
                    && isKeyNotYetExpired(key);
        }
    }

    private boolean isKeyEnabled(Key key) {
        if (key.getAttributes().getEnabled() != null && key.getAttributes().getEnabled()) {
            Log.debugf("The key [%s] is enabled.", key.getKid());
            return true;
        } else {
            Log.warnf("The key [%s] is not enabled.", key.getKid());
            return false;
        }
    }

    private boolean isKeyCreationTimestampCoherent(Key key) {
        long now = Instant.now().getEpochSecond();
        if (key.getAttributes().getCreated() != null && key.getAttributes().getCreated() <= now) {
            Log.debugf("The creation timestamp of [%s] is valid.", key.getKid());
            return true;
        } else {
            Log.warnf("The creation timestamp of [%s] is not valid. Found [%s], expected a value less than [%d].", key.getKid(), key.getAttributes().getCreated(), now);
            return false;
        }
    }

    private boolean isKeyNotYetExpired(Key key) {
        long now = Instant.now().getEpochSecond();
        if (key.getAttributes().getExp() != null && key.getAttributes().getExp() > now) {
            Log.debugf("The key [%s] is not expired.", key.getKid());
            return true;
        } else {
            Log.warnf("The key [%s] is expired. Found [%s], expected a value greater than [%d].", key.getKid(), key.getAttributes().getExp(), now);
            return false;
        }
    }

    private PublicKeyIDPay getPublicKey(KeyDetails key) {
        ArrayList<KeyOp> keyOps = new ArrayList<>();
        keyOps.add(KeyOp.wrapKey);

        if (isKeyValid(key)) {
            KeyNameAndVersion keyNameAndVersion = kidUtil.getNameAndVersionFromAzureKid(key.getKid());
            if (keyNameAndVersion.isValid()) {
                return new PublicKeyIDPay(
                        key.getExponent(),
                        PublicKeyUse.enc,
                        kidUtil.getMyKidFromNameAndVersion(keyNameAndVersion),
                        key.getModulus(),
                        KeyType.RSA,
                        key.getAttributes().getExp(),
                        key.getAttributes().getCreated(),
                        keyOps);
            } else {
                String message = String.format("[%s] Error generating the key pair: kid doesn't contain name and version.", ErrorCode.ERROR_GENERATING_KEY_PAIR);
                Log.fatal(message);
                throw new InternalServerErrorException(Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new Errors(List.of(ErrorCode.ERROR_GENERATING_KEY_PAIR), List.of(ErrorCode.ERROR_GENERATING_KEY_PAIR_MSG)))
                        .build());
            }
        } else {
            String message = String.format("[%s] Error generating the key pair: invalid key pair has been generated.", ErrorCode.ERROR_GENERATING_KEY_PAIR);
            Log.fatal(message);
            throw new InternalServerErrorException(Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Errors(List.of(ErrorCode.ERROR_GENERATING_KEY_PAIR), List.of(ErrorCode.ERROR_GENERATING_KEY_PAIR_MSG)))
                    .build());
        }
    }
}
