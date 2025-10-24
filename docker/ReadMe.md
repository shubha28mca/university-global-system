Docker Compose
Key Notes:

Cassandra: Three DCs (IN, US, EU). Each can hold local data. Replication across DCs can be configured with NetworkTopologyStrategy.

Redis: Separate instances per region. Can be used for ultra-low latency caching.

Kafka: Used for cross-region cache invalidation messages.

Monitoring: Prometheus scrapes metrics; Grafana visualizes them.

TLS placeholders: You can mount certs to Cassandra, Redis, Kafka for production.
