terraform {
  required_providers {
    yandex = {
      source = "yandex-cloud/yandex"
    }
  }
  required_version = ">= 0.13"
}

provider "yandex" {
  zone = "ru-central1-d"
  service_account_key_file = "key.json"
}

resource "yandex_mdb_kafka_cluster" "yuriyradomskiy-cluster" {
  folder_id = "b1gh5kt0tm2akrua7dai"
  name        = "kafka-cluster-123"
  environment = "PRODUCTION"
  network_id  = "enpdfapscr8vmpupl0c8"
  subnet_ids  = ["e2li34pgbv3ptap59t52", "e9bl5hrda0n3qv5ca5oo", "fl8e0ebsvu0cibc404er"]

  config {
    version          = "3.5"
    brokers_count    = 1
    zones            = ["ru-central1-b", "ru-central1-a", "ru-central1-d"]
    assign_public_ip = true
    schema_registry  = true
    kafka {
      resources {
        resource_preset_id = "s3-c2-m8"
        disk_type_id       = "network-ssd"
        disk_size          = 32
      }
      kafka_config {
        compression_type                = "COMPRESSION_TYPE_UNCOMPRESSED"
        log_flush_interval_messages     = 1024
        log_flush_interval_ms           = 1000
        log_flush_scheduler_interval_ms = 1000
        log_retention_bytes             = 1073741824
        log_retention_hours             = 168
        log_retention_minutes           = 10080
        log_retention_ms                = 86400000
        log_segment_bytes               = 134217728
        log_preallocate                 = false
        num_partitions                  = 3
        default_replication_factor      = 3
        message_max_bytes               = 1048588
        replica_fetch_max_bytes         = 1048576
        ssl_cipher_suites               = ["TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256"]
        offsets_retention_minutes       = 10080
        sasl_enabled_mechanisms         = ["SASL_MECHANISM_SCRAM_SHA_256", "SASL_MECHANISM_SCRAM_SHA_512"]
      }
    }
    zookeeper {
      resources {
        resource_preset_id = "s3-c2-m8"
        disk_type_id       = "network-ssd"
        disk_size          = 20
      }
    }
  }
}

resource "yandex_mdb_kafka_topic" "data-products-topic" {
  cluster_id         = yandex_mdb_kafka_cluster.yuriyradomskiy-cluster.id
  name               = "data-products-topic"
  partitions         = 3
  replication_factor = 3
  topic_config {
    cleanup_policy        = "CLEANUP_POLICY_COMPACT"
    compression_type      = "COMPRESSION_TYPE_SNAPPY"
    delete_retention_ms   = 86400000
    file_delete_delay_ms  = 60000
    flush_messages        = 128
    flush_ms              = 1000
    min_compaction_lag_ms = 0
    retention_bytes       = 10737418240
    retention_ms          = 604800000
    max_message_bytes     = 1048588
    min_insync_replicas   = 2
    segment_bytes         = 268435456
    preallocate           = false
  }
}

resource "yandex_mdb_kafka_topic" "data-client-requests-topic" {
  cluster_id         = yandex_mdb_kafka_cluster.yuriyradomskiy-cluster.id
  name               = "data-client-requests-topic"
  partitions         = 3
  replication_factor = 3
  topic_config {
    cleanup_policy        = "CLEANUP_POLICY_COMPACT"
    compression_type      = "COMPRESSION_TYPE_SNAPPY"
    delete_retention_ms   = 86400000
    file_delete_delay_ms  = 60000
    flush_messages        = 128
    flush_ms              = 1000
    min_compaction_lag_ms = 0
    retention_bytes       = 10737418240
    retention_ms          = 604800000
    max_message_bytes     = 1048588
    min_insync_replicas   = 2
    segment_bytes         = 268435456
    preallocate           = false
  }
}

resource "yandex_mdb_kafka_user" "shop-user" {
  cluster_id = yandex_mdb_kafka_cluster.yuriyradomskiy-cluster.id
  name       = "shop-user"
  password   = "shop-user-password"
  permission {
    topic_name  = "data-products-topic"
    role        = "ACCESS_ROLE_PRODUCER"
  }
}

resource "yandex_mdb_kafka_user" "client-user" {
  cluster_id = yandex_mdb_kafka_cluster.yuriyradomskiy-cluster.id
  name       = "client-user"
  password   = "client-user-password"
  permission {
    topic_name  = "data-client-requests-topic"
    role        = "ACCESS_ROLE_PRODUCER"
  }
}

resource "yandex_mdb_kafka_user" "connector-user" {
  cluster_id = yandex_mdb_kafka_cluster.yuriyradomskiy-cluster.id
  name       = "connector-user"
  password   = "connector-user-password"
  permission {
    topic_name  = "data-client-requests-topic"
    role        = "ACCESS_ROLE_CONSUMER"
  }
  permission {
    topic_name  = "data-products-topic"
    role        = "ACCESS_ROLE_CONSUMER"
  }
}
