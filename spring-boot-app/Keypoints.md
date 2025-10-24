Key Points:

Cassandra:

Each region connects to its local datacenter.

Keyspace is the same (university_keyspace) for multi-region replication.

Redis:

Each region uses its local Redis instance for caching.

Ports match those defined in Docker Compose.

Kafka:

All regions connect to single Kafka cluster.

Separate consumer groups per region (university-group-in, -us, -eu).

Prometheus metrics:

Enabled via Spring Boot Actuator.

/actuator/prometheus endpoint ready for scraping.

Ports:

8080 → IN

8081 → US

8082 → EU
