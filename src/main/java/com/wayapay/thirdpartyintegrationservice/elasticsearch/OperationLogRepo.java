package com.wayapay.thirdpartyintegrationservice.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperationLogRepo extends ElasticsearchRepository<OperationLog, String> {

    Optional<OperationLog> findByTransactionId(String transactionId);

}
