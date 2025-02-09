package practicum.kafka.sprint.four;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static practicum.kafka.sprint.four.PrometheusSinkConnector.LISTENER_PATH;
import static practicum.kafka.sprint.four.PrometheusSinkConnector.LISTENER_PORT;
import static practicum.kafka.sprint.four.PrometheusSinkConnector.METRIC_NAMES;

public class PrometheusSinkTask extends SinkTask {

    private static final Logger log = LoggerFactory.getLogger(PrometheusSinkTask.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private PrometheusHttpServer httpServer;
    private List<String> metricNames;

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public void start(Map<String, String> map) {
        String path = map.get(LISTENER_PATH);
        int port = Integer.parseInt(map.get(LISTENER_PORT));
        httpServer = PrometheusHttpServer.getInstance(path, port);
        metricNames = Arrays.stream(map.get(METRIC_NAMES).split(",")).toList();
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
            var recordValue = objectMapper.readTree(record.value().toString());
            var metrics = objectMapper.treeToValue(
                    recordValue,
                    new TypeReference<Map<String, MetricEvent>>() {
                    });
            metricNames.forEach(name -> httpServer.addMetric(name, metrics.get(name)));
        } catch (Exception e) {
            log.error("Unable to parse record: {}", e.getMessage(), e);
        }
    }

}
