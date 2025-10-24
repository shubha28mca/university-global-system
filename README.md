# university-global-system

Create the Casandra DB (NameSpace) and Table using below Codes.

full multi-region Cassandra keyspace and student table setup compatible with your Spring Boot project. We’ll use NetworkTopologyStrategy for IN_DC, US_DC, EU_DC and proper partitioning.

1. Create the keyspace with multi-DC replication
   
```cql
-- Create keyspace for University Global System
-- Replicates data across India, US, EU datacenters
CREATE KEYSPACE IF NOT EXISTS university_keyspace
WITH replication = {
  'class': 'NetworkTopologyStrategy',
  'IN_DC': 3,   -- Replicate 3 nodes in India DC
  'US_DC': 3,   -- Replicate 3 nodes in US DC
  'EU_DC': 3    -- Replicate 3 nodes in EU DC
}
AND durable_writes = true;

-- Notes:
-- 1. Adjust replication factor per DC based on number of nodes.
-- 2. durable_writes = true ensures writes are persisted to commit log.
-- 3. Make sure your Cassandra nodes are correctly labeled with DCs:
--    e.g., cassandra-in -> IN_DC, cassandra-us -> US_DC, cassandra-eu -> EU_DC
```

2. Create the Student table with partitioning

```cql
-- Create student table
CREATE TABLE IF NOT EXISTS university_keyspace.student (
    student_id uuid,
    department text,
    year int,
    name text,
    PRIMARY KEY ((department, year), student_id)
) WITH comment = 'Student table partitioned by department and year'
   AND compaction = {'class': 'SizeTieredCompactionStrategy'};

-- Notes:
-- 1. Partition key: (department, year)
--    - Ensures all students of the same department & admission year are in same partition.
--    - Helps with queries like "get all students of CS department for 2025".
-- 2. Clustering column: student_id
--    - Ensures uniqueness of student within the partition.
-- 3. Compaction strategy: SizeTieredCompactionStrategy (default)
--    - Can switch to LeveledCompactionStrategy if there are many updates.
-- 4. Local reads are served by nodes in the same DC.
--    Cross-region reads go to remote DC if needed (higher latency).
```


3. Example Queries

```cql
-- Insert a student
INSERT INTO university_keyspace.student (student_id, department, year, name)
VALUES (uuid(), 'CS', 2025, 'Alice Johnson');

-- Select student by partition key
SELECT * FROM university_keyspace.student
WHERE department = 'CS' AND year = 2025;

-- Select by primary key
SELECT * FROM university_keyspace.student
WHERE department = 'CS' AND year = 2025 AND student_id = <UUID>;

-- Update student
UPDATE university_keyspace.student
SET name = 'Alice Smith'
WHERE department = 'CS' AND year = 2025 AND student_id = <UUID>;

-- Delete student
DELETE FROM university_keyspace.student
WHERE department = 'CS' AND year = 2025 AND student_id = <UUID>;
```

✅ Key Notes for Multi-Region Setup

NetworkTopologyStrategy:

Each DC has its own replication factor.

Cassandra ensures eventual consistency across DCs.

Partitioning:

(department, year) ensures students are grouped by department/year, improving query efficiency.

student_id as clustering column ensures unique rows in the partition.

Read/Write Behavior:

Reads/writes are local DC first (low latency).

Cross-region reads incur higher latency.

Can configure consistency levels per operation: LOCAL_QUORUM, QUORUM, ALL.

Integration with Spring Boot:

spring.data.cassandra.keyspace-name=university_keyspace

spring.data.cassandra.local-datacenter=<region_DC>

Repository layer handles CRUD operations automatically.




Notes & Suggestions:

Circuit Breakers: Implemented using Resilience4j annotations. This prevents cascading failures if Cassandra or Redis is down.

Redis caching: Stores per-region cache for fast reads. TTL = 10 mins (adjustable).

Kafka cross-region invalidation: Ensures that other regions purge stale cache after updates.

Fallbacks: Each service method has a fallback; can be improved to return stale cache or a custom error.

Scalability:

Redis can be clustered per region.

Kafka can be multi-broker, multi-region for durability.

Cassandra already configured for multi-DC replication.

Improvements:

Add DTOs for API layer instead of exposing entity directly.

Add validation for input.

Add metrics for cache hit/miss to Prometheus.

Integrate Spring Cloud Sleuth + Jaeger for request tracing across regions.
