package com.wayapay.thirdpartyintegrationservice.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationLogRepo extends ElasticsearchRepository<OperationLog, String> {

}
