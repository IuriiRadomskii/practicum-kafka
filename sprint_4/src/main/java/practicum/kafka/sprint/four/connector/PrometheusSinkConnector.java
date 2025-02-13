package practicum.kafka.sprint.four.connector;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrometheusSinkConnector extends SinkConnector {

    private static final Logger log = LoggerFactory.getLogger(PrometheusSinkConnector.class);
    public static final String LISTENER_PATH = "prometheus.listener.path";
    public static final String LISTENER_PORT = "prometheus.listener.port";
    private Map<String, String> props;

    @Override
    public void start(Map<String, String> props) {
        log.info("Starting Prometheus Sink Connector: {}", props);
        this.props = props;
    }

    @Override
    public Class<? extends Task> taskClass() {
        log.info("Connector taskClass()");
        return PrometheusSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        log.info("Connector taskConfigs({})", maxTasks);
        List<Map<String, String>> configs = new ArrayList<>();
        for (int i = 0; i < maxTasks; i++) {
            configs.add(Map.of(
                    LISTENER_PATH, props.get(LISTENER_PATH),
                    LISTENER_PORT, props.get(LISTENER_PORT))
            );
        }
        return configs;
    }

    @Override
    public void stop() {
        //ignored
    }

    @Override
    public ConfigDef config() {
        log.info("Connector config()");
        //skip validation for some reason
        return new ConfigDef();
    }

    @Override
    public String version() {
        log.info("Connector version()");
        return "1.0.0";
    }

}
