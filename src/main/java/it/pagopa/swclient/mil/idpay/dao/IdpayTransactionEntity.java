package it.pagopa.swclient.mil.idpay.dao;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

@MongoEntity(database = "mil", collection = "idpayTransactions")
public class IdpayTransactionEntity {

    @BsonId
    public String transactionId;

    public IdpayTransaction idpayTransaction;
}
