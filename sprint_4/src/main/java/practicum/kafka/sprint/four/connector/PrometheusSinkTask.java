package practicum.kafka.sprint.four.connector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

import static practicum.kafka.sprint.four.connector.PrometheusSinkConnector.LISTENER_PATH;
import static practicum.kafka.sprint.four.connector.PrometheusSinkConnector.LISTENER_PORT;

public class PrometheusSinkTask extends SinkTask {

    private static final Logger log = LoggerFactory.getLogger(PrometheusSinkTask.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private PrometheusHttpServer httpServer;

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public void start(Map<String, String> map) {
        String path = map.get(LISTENER_PATH);
        int port = Integer.parseInt(map.get(LISTENER_PORT));
        httpServer = PrometheusHttpServer.getInstance(path, port);
    }

    @Override
    public void put(Collection<SinkRecord> collection) {
        collection.forEach(this::handleRecord);
    }

    @Override
    public void stop() {

    }

    private void handleRecord(SinkRecord record) {
        try {
            log.info("Raw message: {}", record.value().toString());
            var recordValue = objectMapper.readTree(record.value().toString());
            var metrics = objectMapper.treeToValue(
                    recordValue,
                    new TypeReference<Map<String, MetricEvent>>() {
                    });
            metrics.forEach((k, v) -> {
                if (isValid(k, v)) {
                    httpServer.addMetric(k, v);
                    log.info("Metric {} nandled", v);
                }
            });
        } catch (Exception e) {
            log.error("Unable to parse record: {}", e.getMessage(), e);
        }
    }

    private boolean isValid(String key, MetricEvent event) {
        if (key != null
                && event != null
                && event.description() != null
                && event.type() != null) {
            return true;
        }
        return false;
    }

}
