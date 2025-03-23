package practicum.kafka.sprint.six;

import java.util.*;

import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.producer.*;

public class CloudProducer {

  public static void main(String[] args) {

    int MSG_COUNT = 5;

    String HOST = "rc1a-qamfgm7vrpt2t3hr.mdb.yandexcloud.net:9091";
    String TOPIC = "cluster-topic";
    String USER = "admin-kafka";
    String PASS = "admin-kafka-password";
    String TS_FILE = "C:\\jdk\\java17\\lib\\security\\cacerts";
    String TS_PASS = "changeit";

    String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
    String jaasCfg = String.format(jaasTemplate, USER, PASS);
    String KEY = "key";

    Properties props = new Properties();
    props.put("bootstrap.servers", HOST);
    props.put("acks", "all");
    props.put("key.serializer", StringSerializer.class.getName());
    props.put("value.serializer", StringSerializer.class.getName());
    props.put("security.protocol", "SASL_SSL");
    props.put("sasl.mechanism", "SCRAM-SHA-512");
    props.put("sasl.jaas.config", jaasCfg);
    props.put("ssl.truststore.location", TS_FILE);
    props.put("ssl.truststore.password", TS_PASS);

    Producer<String, String> producer = new KafkaProducer<>(props);

    try {
     for (int i = 1; i <= MSG_COUNT; i++){
       producer.send(new ProducerRecord<String, String>(TOPIC, KEY + i, "test message" + i)).get();
       System.out.println("Test message " + i);
      }
     producer.flush();
     producer.close();
    } catch (Exception ex) {
        System.out.println(ex);
        producer.close();
    }
  }
}
