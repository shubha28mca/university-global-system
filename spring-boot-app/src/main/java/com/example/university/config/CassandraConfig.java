package com.example.university.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.cassandra.core.CassandraTemplate;

@Configuration
@EnableCassandraRepositories(basePackages = "com.example.university.repo")
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Value("${spring.data.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.data.cassandra.port}")
    private int port;

    @Value("${spring.data.cassandra.keyspace-name}")
    private String keyspaceName;

    @Value("${spring.data.cassandra.local-datacenter}")
    private String localDatacenter;

    @Override
    protected String getKeyspaceName() {
        return keyspaceName;
    }

    @Override
    protected String getContactPoints() {
        return contactPoints;
    }

    @Override
    protected int getPort() {
        return port;
    }

    @Override
    protected String getLocalDataCenter() {
        return localDatacenter;
    }

    // CassandraTemplate for operations beyond repository
    @Bean
    public CassandraTemplate cassandraTemplate() throws Exception {
        return new CassandraTemplate(session().getObject());
    }

    // Notes:
    // - For TLS, you can configure SSL options using ClusterBuilderConfigurer.
    // - Multi-DC replication is configured in CQL keyspace with NetworkTopologyStrategy.
}
