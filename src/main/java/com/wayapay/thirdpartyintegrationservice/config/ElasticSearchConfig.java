package com.wayapay.thirdpartyintegrationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.wayapay.thirdpartyintegrationservice.elasticsearch")
public class ElasticSearchConfig {

}