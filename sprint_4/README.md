# Check custom connector

### Create kafka network
```bash
docker network create custom_network
```

### Up kafka cluster
```bash
docker compose -p sprint_4_custom_connector -f ./infra/docker-compose.yaml up -d
```

### !Wait for kafka cluster is up and running!

### Create kafka topic: get container_id of kafka-0-1 service. Use 'docker ps' command for it
```bash
docker exec -it b8c9d204551f /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic metrics-event-topic --bootstrap-server kafka-0:9092 --partitions 1 --replication-factor 1
```

### Run CustomMetricsProducer class
### Run PUT request in request.http file in resources folder
### Open Grafana: http://localhost:3000
### Connector port is exposed to localhost, so you can check metric manually on http://localhost:8384/metrics

# Check JDBC Connector

### Create kafka topic: get container_id of kafka-0-1 service. Use 'docker ps' command for it
```bash
docker exec -it b066e141dd27 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic postgresql-jdbc-bulk-users --bootstrap-server kafka-0:9092 --partitions 1 --replication-factor 1
```

### Create users table in customers schema
```SQL
-- create table
create table users
(
    id           integer                             not null
        primary key,
    name         varchar(255),
    private_info varchar,
    updated_at   timestamp default CURRENT_TIMESTAMP not null
);

alter table users
    owner to "postgres-user";

-- add random data avg entry size 35-40 bytes
DO
$$
    DECLARE
iter integer;
BEGIN
FOR iter IN 1000..10000
            LOOP
                INSERT INTO users (id, name, private_info)
                VALUES (iter, 'Name_' || iter::text, 'private_info_' || iter::text);
END LOOP;
END
$$;
```
### http-requests to run jdbc-connector are presented in resources/jdbc-connector-requests.http file

### Jdbc connector experiments on 200k entries
1. "producer.override.linger.ms": 1000,
   "producer.override.batch.size": 500,
   "poll.interval.ms": "200",
   "compression.type": "snappy",
   "batch.max.rows": 100,
   "batch.size": 4100,
   "linger.ms": "200",
   "buffer.memory": "33554432" - this config killed my pc, it was needed to reboot

2. "producer.override.linger.ms": 1000,
   "producer.override.batch.size": 1000,
   "compression.type": "snappy",
   "batch.max.rows": 500,
   "poll.interval.ms": 250,
   "batch.size": 25000,
   "linger.ms": 2500,
   "buffer.memory": 33554432 - and this one worked pretty well. All entries were processed in 4-5 seconds

# Check Debezium connector

### Create kafka topic: get container_id of kafka-0-1 service. Use 'docker ps' command for it
```bash
docker exec -it b066e141dd27 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic customers.public.users --bootstrap-server kafka-0:9092 --partitions 1 --replication-factor 1
```

### Debezium log
```log
2025-02-13 21:15:39 16:15:39.926 INFO  i.d.config.CommonConnectorConfig - Loading the custom source info struct maker plugin: io.debezium.connector.postgresql.PostgresSourceInfoStructMaker
2025-02-13 21:15:39 16:15:39.927 INFO  i.d.config.CommonConnectorConfig - Loading the custom topic naming strategy plugin: io.debezium.schema.SchemaTopicNamingStrategy
2025-02-13 21:15:39 16:15:39.938 INFO  io.debezium.jdbc.JdbcConnection - Connection gracefully closed
2025-02-13 21:15:40 16:15:40.007 WARN  i.d.c.postgresql.TypeRegistry - Type [oid:13529, name:_pg_user_mappings] is already mapped
2025-02-13 21:15:40 16:15:40.007 WARN  i.d.c.postgresql.TypeRegistry - Type [oid:13203, name:cardinal_number] is already mapped
2025-02-13 21:15:40 16:15:40.007 WARN  i.d.c.postgresql.TypeRegistry - Type [oid:13206, name:character_data] is already mapped
2025-02-13 21:15:40 16:15:40.007 WARN  i.d.c.postgresql.TypeRegistry - Type [oid:13208, name:sql_identifier] is already mapped
2025-02-13 21:15:40 16:15:40.007 WARN  i.d.c.postgresql.TypeRegistry - Type [oid:13214, name:time_stamp] is already mapped
2025-02-13 21:15:40 16:15:40.007 WARN  i.d.c.postgresql.TypeRegistry - Type [oid:13216, name:yes_or_no] is already mapped
2025-02-13 21:15:40 16:15:40.078 INFO  i.d.connector.common.BaseSourceTask - No previous offsets found
2025-02-13 21:15:40 16:15:40.126 WARN  i.d.c.postgresql.TypeRegistry - Type [oid:13529, name:_pg_user_mappings] is already mapped
2025-02-13 21:15:40 16:15:40.126 WARN  i.d.c.postgresql.TypeRegistry - Type [oid:13203, name:cardinal_number] is already mapped
2025-02-13 21:15:40 16:15:40.126 WARN  i.d.c.postgresql.TypeRegistry - Type [oid:13206, name:character_data] is already mapped
2025-02-13 21:15:40 16:15:40.126 WARN  i.d.c.postgresql.TypeRegistry - Type [oid:13208, name:sql_identifier] is already mapped
2025-02-13 21:15:40 16:15:40.126 WARN  i.d.c.postgresql.TypeRegistry - Type [oid:13214, name:time_stamp] is already mapped
2025-02-13 21:15:40 16:15:40.126 WARN  i.d.c.postgresql.TypeRegistry - Type [oid:13216, name:yes_or_no] is already mapped
2025-02-13 21:15:40 16:15:40.171 INFO  i.d.connector.common.BaseSourceTask - Connector started for the first time.
2025-02-13 21:15:40 16:15:40.173 INFO  i.d.c.p.PostgresConnectorTask - No previous offset found
2025-02-13 21:15:40 16:15:40.178 INFO  i.d.c.p.PostgresConnectorTask - user 'postgres-user' connected to database 'customers' on PostgreSQL 16.4 (Debian 16.4-1.pgdg110+2) on x86_64-pc-linux-gnu, compiled by gcc (Debian 10.2.1-6) 10.2.1 20210110, 64-bit with roles:
2025-02-13 21:15:40     role 'pg_read_all_settings' [superuser: false, replication: false, inherit: true, create role: false, create db: false, can log in: false]
2025-02-13 21:15:40     role 'pg_database_owner' [superuser: false, replication: false, inherit: true, create role: false, create db: false, can log in: false]
2025-02-13 21:15:40     role 'pg_stat_scan_tables' [superuser: false, replication: false, inherit: true, create role: false, create db: false, can log in: false]
2025-02-13 21:15:40     role 'pg_checkpoint' [superuser: false, replication: false, inherit: true, create role: false, create db: false, can log in: false]
2025-02-13 21:15:40     role 'pg_write_server_files' [superuser: false, replication: false, inherit: true, create role: false, create db: false, can log in: false]
2025-02-13 21:15:40     role 'pg_use_reserved_connections' [superuser: false, replication: false, inherit: true, create role: false, create db: false, can log in: false]
2025-02-13 21:15:40     role 'postgres-user' [superuser: true, replication: true, inherit: true, create role: true, create db: true, can log in: true]
2025-02-13 21:15:40     role 'pg_read_all_data' [superuser: false, replication: false, inherit: true, create role: false, create db: false, can log in: false]
2025-02-13 21:15:40     role 'pg_write_all_data' [superuser: false, replication: false, inherit: true, create role: false, create db: false, can log in: false]
2025-02-13 21:15:40     role 'pg_monitor' [superuser: false, replication: false, inherit: true, create role: false, create db: false, can log in: false]
2025-02-13 21:15:40     role 'pg_read_server_files' [superuser: false, replication: false, inherit: true, create role: false, create db: false, can log in: false]
2025-02-13 21:15:40     role 'pg_create_subscription' [superuser: false, replication: false, inherit: true, create role: false, create db: false, can log in: false]
2025-02-13 21:15:40     role 'pg_execute_server_program' [superuser: false, replication: false, inherit: true, create role: false, create db: false, can log in: false]
2025-02-13 21:15:40     role 'pg_read_all_stats' [superuser: false, replication: false, inherit: true, create role: false, create db: false, can log in: false]
2025-02-13 21:15:40     role 'pg_signal_backend' [superuser: false, replication: false, inherit: true, create role: false, create db: false, can log in: false]
2025-02-13 21:15:40 16:15:40.186 INFO  i.d.c.p.c.PostgresConnection - Obtained valid replication slot ReplicationSlot [active=false, latestFlushedLsn=null, catalogXmin=null]
2025-02-13 21:15:40 16:15:40.211 INFO  i.d.c.p.c.PostgresReplicationConnection - Creating replication slot with command CREATE_REPLICATION_SLOT "debezium"  LOGICAL decoderbufs 
2025-02-13 21:15:40 16:15:40.270 INFO  io.debezium.util.Threads - Requested thread factory for component PostgresConnector, id = customers named = SignalProcessor
2025-02-13 21:15:40 16:15:40.293 INFO  io.debezium.util.Threads - Requested thread factory for component PostgresConnector, id = customers named = change-event-source-coordinator
2025-02-13 21:15:40 16:15:40.293 INFO  io.debezium.util.Threads - Requested thread factory for component PostgresConnector, id = customers named = blocking-snapshot
2025-02-13 21:15:40 16:15:40.303 INFO  io.debezium.util.Threads - Creating thread debezium-postgresconnector-customers-change-event-source-coordinator
2025-02-13 21:15:40 16:15:40.317 INFO  i.d.p.ChangeEventSourceCoordinator - Metrics registered
2025-02-13 21:15:40 16:15:40.317 INFO  i.d.p.ChangeEventSourceCoordinator - Context created
2025-02-13 21:15:40 16:15:40.326 INFO  i.d.c.p.PostgresSnapshotChangeEventSource - According to the connector configuration data will be snapshotted
2025-02-13 21:15:40 16:15:40.330 INFO  i.d.r.RelationalSnapshotChangeEventSource - Snapshot step 1 - Preparing
2025-02-13 21:15:40 16:15:40.331 INFO  i.d.c.p.PostgresSnapshotChangeEventSource - Setting isolation level
2025-02-13 21:15:40 16:15:40.331 INFO  i.d.c.p.PostgresSnapshotChangeEventSource - Opening transaction with statement SET TRANSACTION ISOLATION LEVEL REPEATABLE READ; 
2025-02-13 21:15:40 SET TRANSACTION SNAPSHOT '00000006-00000002-1';
2025-02-13 21:15:40 16:15:40.415 INFO  i.d.r.RelationalSnapshotChangeEventSource - Snapshot step 2 - Determining captured tables
2025-02-13 21:15:40 16:15:40.418 INFO  i.d.r.RelationalSnapshotChangeEventSource - Adding table public.users to the list of capture schema tables
2025-02-13 21:15:40 16:15:40.419 INFO  i.d.r.RelationalSnapshotChangeEventSource - Created connection pool with 1 threads
2025-02-13 21:15:40 16:15:40.419 INFO  i.d.r.RelationalSnapshotChangeEventSource - Snapshot step 3 - Locking captured tables [public.users]
2025-02-13 21:15:40 16:15:40.423 INFO  i.d.r.RelationalSnapshotChangeEventSource - Snapshot step 4 - Determining snapshot offset
2025-02-13 21:15:40 16:15:40.426 INFO  i.d.c.p.PostgresOffsetContext - Creating initial offset context
2025-02-13 21:15:40 16:15:40.430 INFO  i.d.c.p.PostgresOffsetContext - Read xlogStart at 'LSN{0/7BBFD88}' from transaction '773'
2025-02-13 21:15:40 16:15:40.436 INFO  i.d.c.p.PostgresSnapshotChangeEventSource - Read xlogStart at 'LSN{0/7BBFD88}' from transaction '773'
2025-02-13 21:15:40 16:15:40.436 INFO  i.d.r.RelationalSnapshotChangeEventSource - Snapshot step 5 - Reading structure of captured tables
2025-02-13 21:15:40 16:15:40.436 INFO  i.d.c.p.PostgresSnapshotChangeEventSource - Reading structure of schema 'public' of catalog 'customers'
2025-02-13 21:15:40 16:15:40.459 INFO  i.d.r.RelationalSnapshotChangeEventSource - Snapshot step 6 - Persisting schema history
2025-02-13 21:15:40 16:15:40.460 INFO  i.d.r.RelationalSnapshotChangeEventSource - Snapshot step 7 - Snapshotting data
2025-02-13 21:15:40 16:15:40.462 INFO  i.d.r.RelationalSnapshotChangeEventSource - Creating snapshot worker pool with 1 worker thread(s)
2025-02-13 21:15:40 16:15:40.464 INFO  i.d.r.RelationalSnapshotChangeEventSource - For table 'public.users' using select statement: 'SELECT "id", "name", "private_info", "updated_at" FROM "public"."users"'
2025-02-13 21:15:40 16:15:40.469 INFO  i.d.r.RelationalSnapshotChangeEventSource - Exporting data from table 'public.users' (1 of 1 tables)
2025-02-13 21:15:40 16:15:40.503 INFO  i.d.r.RelationalSnapshotChangeEventSource -       Finished exporting 100 records for table 'public.users' (1 of 1 tables); total duration '00:00:00.034'
2025-02-13 21:15:40 16:15:40.506 INFO  i.d.p.s.AbstractSnapshotChangeEventSource - Snapshot - Final stage
2025-02-13 21:15:40 16:15:40.506 INFO  i.d.p.s.AbstractSnapshotChangeEventSource - Snapshot completed
2025-02-13 21:15:40 16:15:40.507 INFO  i.d.p.ChangeEventSourceCoordinator - Snapshot ended with SnapshotResult [status=COMPLETED, offset=PostgresOffsetContext [sourceInfoSchema=Schema{io.debezium.connector.postgresql.Source:STRUCT}, sourceInfo=source_info[server='customers'db='customers', lsn=LSN{0/7BBFD88}, txId=773, timestamp=2025-02-13T16:15:40.333087Z, snapshot=FALSE, schema=public, table=users], lastSnapshotRecord=true, lastCompletelyProcessedLsn=null, lastCommitLsn=null, streamingStoppingLsn=null, transactionContext=TransactionContext [currentTransactionId=null, perTableEventCount={}, totalEventCount=0], incrementalSnapshotContext=IncrementalSnapshotContext [windowOpened=false, chunkEndPosition=null, dataCollectionsToSnapshot=[], lastEventKeySent=null, maximumKey=null]]]
2025-02-13 21:15:40 16:15:40.520 INFO  i.d.p.ChangeEventSourceCoordinator - Connected metrics set to 'true'
2025-02-13 21:15:40 16:15:40.543 INFO  i.d.c.postgresql.PostgresSchema - REPLICA IDENTITY for 'public.users' is 'DEFAULT'; UPDATE and DELETE events will contain previous values only for PK columns
2025-02-13 21:15:40 16:15:40.554 INFO  i.d.pipeline.signal.SignalProcessor - SignalProcessor started. Scheduling it every 5000ms
2025-02-13 21:15:40 16:15:40.555 INFO  io.debezium.util.Threads - Creating thread debezium-postgresconnector-customers-SignalProcessor
2025-02-13 21:15:40 16:15:40.555 INFO  i.d.p.ChangeEventSourceCoordinator - Starting streaming
2025-02-13 21:15:40 16:15:40.555 INFO  i.d.c.p.PostgresStreamingChangeEventSource - Retrieved latest position from stored offset 'LSN{0/7BBFD88}'
2025-02-13 21:15:40 16:15:40.556 INFO  i.d.c.p.c.WalPositionLocator - Looking for WAL restart position for last commit LSN 'null' and last change LSN 'LSN{0/7BBFD88}'
2025-02-13 21:15:40 16:15:40.565 INFO  i.d.c.p.c.PostgresConnection - Obtained valid replication slot ReplicationSlot [active=false, latestFlushedLsn=LSN{0/7BBFD88}, catalogXmin=773]
2025-02-13 21:15:40 16:15:40.566 INFO  io.debezium.jdbc.JdbcConnection - Connection gracefully closed
2025-02-13 21:15:40 16:15:40.584 INFO  io.debezium.util.Threads - Requested thread factory for component PostgresConnector, id = customers named = keep-alive
2025-02-13 21:15:40 16:15:40.584 INFO  io.debezium.util.Threads - Creating thread debezium-postgresconnector-customers-keep-alive
2025-02-13 21:15:40 16:15:40.603 INFO  i.d.c.postgresql.PostgresSchema - REPLICA IDENTITY for 'public.users' is 'DEFAULT'; UPDATE and DELETE events will contain previous values only for PK columns
2025-02-13 21:15:40 16:15:40.604 INFO  i.d.c.p.PostgresStreamingChangeEventSource - Processing messages
2025-02-13 21:15:41 16:15:41.456 WARN  o.a.k.clients.admin.KafkaAdminClient - [AdminClient clientId=connector-adminclient-pg-connector-0] The DescribeTopicPartitions API is not supported, using Metadata API to describe topics.
2025-02-13 21:15:41 16:15:41.582 WARN  o.apache.kafka.clients.NetworkClient - [Producer clientId=connector-producer-pg-connector-0] Error while fetching metadata with correlation id 4 : {customers.public.users=UNKNOWN_TOPIC_OR_PARTITION}
2025-02-13 21:15:42 16:15:42.377 WARN  org.apache.kafka.connect.util.Stage - Ignoring invalid completion time 1739463342377 since it is before this stage's start time of 1739463342437
2025-02-13 21:16:11 16:16:11.836 WARN  org.apache.kafka.connect.util.Stage - Ignoring invalid completion time 1739463371836 since it is before this stage's start time of 1739463372222
2025-02-13 21:16:39 16:16:39.291 INFO  i.d.connector.common.BaseSourceTask - Found previous partition offset PostgresPartition [sourcePartition={server=customers}]: {lsn=129760648, txId=773, ts_usec=1739463340333087}
2025-02-13 21:16:41 16:16:41.801 WARN  org.apache.kafka.connect.util.Stage - Ignoring invalid completion time 1739463401801 since it is before this stage's start time of 1739463401826
2025-02-13 21:17:11 16:17:11.581 WARN  org.apache.kafka.connect.util.Stage - Ignoring invalid completion time 1739463431581 since it is before this stage's start time of 1739463431632
```