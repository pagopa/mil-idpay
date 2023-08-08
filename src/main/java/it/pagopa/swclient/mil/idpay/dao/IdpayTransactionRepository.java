package it.pagopa.swclient.mil.idpay.dao;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IdpayTransactionRepository implements ReactivePanacheMongoRepositoryBase<IdpayTransactionEntity, String> {
}
