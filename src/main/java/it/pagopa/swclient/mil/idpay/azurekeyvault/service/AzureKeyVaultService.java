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
                        } else if (error != null || (getKeyResponse != null && (!isKeyValid(getKeyResponse) || !isKeyNotYetExpired(getKeyResponse.getDetails().getKid(), getKeyResponse.getAttributes())))) {//Se NOT FOUND o Expired chiamo CreateKey
                            return createAzureKVKey(accessToken, keyName);
                        } else {
                            return Uni.createFrom().item(getPublicKey(getKeyResponse));
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
                .map(this::getPublicKey);
    }

    private boolean isKeyValid(DetailedKey key) {
        if (key.getDetails() != null) {
            return isKeyValid(key.getDetails().getKid(), key.getAttributes()) && isKeyTypeRsa(key.getDetails()) && isKeySuitableForWrap(key.getDetails());
        } else {
            Log.warn("Received key without details.");
            return false;
        }
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

    /**
     * @param kid
     * @param attributes
     * @return
     */
    private boolean isKeyValid(String kid, KeyAttributes attributes) {
        if (attributes == null) {
            Log.errorf("The key [%s] has null attributes.", kid);
            return false;
        } else {
            return isKeyEnabled(kid, attributes)
                    && isKeyCreationTimestampCoherent(kid, attributes)
                    && isKeyNotYetExpired(kid, attributes)
                    && isKeyNotBeforeMet(kid, attributes);
        }
    }

    private boolean isKeyEnabled(String kid, KeyAttributes attributes) {
        if (attributes.getEnabled() != null && attributes.getEnabled()) {
            Log.debugf("The key [%s] is enabled.", kid);
            return true;
        } else {
            Log.warnf("The key [%s] is not enabled.", kid);
            return false;
        }
    }

    private boolean isKeyCreationTimestampCoherent(String kid, KeyAttributes attributes) {
        long now = Instant.now().getEpochSecond();
        if (attributes.getCreated() != null && attributes.getCreated() <= now) {
            Log.debugf("The creation timestamp of [%s] is valid.", kid);
            return true;
        } else {
            Log.warnf("The creation timestamp of [%s] is not valid. Found [%s], expected a value less than [%d].", kid, attributes.getCreated(), now);
            return false;
        }
    }

    private boolean isKeyNotYetExpired(String kid, KeyAttributes attributes) {
        long now = Instant.now().getEpochSecond();
        if (attributes.getExp() != null && attributes.getExp() > now) {
            Log.debugf("The key [%s] is not expired.", kid);
            return true;
        } else {
            Log.warnf("The key [%s] is expired. Found [%s], expected a value greater than [%d].", kid, attributes.getExp(), now);
            return false;
        }
    }

    private boolean isKeyNotBeforeMet(String kid, KeyAttributes attributes) {
        long now = Instant.now().getEpochSecond();
        if (attributes.getNbf() != null && attributes.getNbf() <= now) {
            Log.debugf("The 'not before' timestamp of [%s] is valid.", kid);
            return true;
        } else {
            Log.warnf("The 'not before' timestamp of [%s] is not valid. Found [%s], expected a value less than [%d].", kid, attributes.getNbf(), now);
            return false;
        }
    }

    private PublicKeyIDPay getPublicKey(DetailedKey key) {
        ArrayList<KeyOp> keyOps = new ArrayList<>();
        keyOps.add(KeyOp.wrapKey);

        if (isKeyValid(key)) {
            KeyNameAndVersion keyNameAndVersion = kidUtil.getNameAndVersionFromAzureKid(key.getDetails().getKid());

            return new PublicKeyIDPay(
                    key.getDetails().getExponent(),
                    PublicKeyUse.enc,
                    kidUtil.getMyKidFromNameAndVersion(keyNameAndVersion),
                    key.getDetails().getModulus(),
                    KeyType.RSA,
                    key.getAttributes().getExp(),
                    key.getAttributes().getCreated(),
                    keyOps);
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
