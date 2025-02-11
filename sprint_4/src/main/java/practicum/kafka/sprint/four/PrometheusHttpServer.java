/*
package practicum.kafka.sprint.four;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PrometheusHttpServer {

    private static final Logger log = LoggerFactory.getLogger(PrometheusHttpServer.class.getName());
    private static final String PROMETHEUS_METRIC_TEMPLATE = "# HELP %s %s\n# TYPE %s %s\n%s %s";
    private static volatile PrometheusHttpServer instance;
    private final Map<String, Queue<String>> buffer = new ConcurrentHashMap<>();
    private Server server;

    private final int port;
    private final String path;

    public static PrometheusHttpServer getInstance(final String path, final int port) {
        if (instance == null) {
            synchronized (PrometheusHttpServer.class) {
                if (instance == null) {
                    instance = new PrometheusHttpServer(path, port);
                    instance.start();
                }
            }
        }
        return instance;
    }

    private PrometheusHttpServer(String path, int port) {
        this.port = port;
        this.path = path;
    }

    private void start() {
        var runnerThread = new Thread(() -> {
            log.info("Starting prometheus http server on port {}", port);
            server = new Server(port);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            context.addServlet(new ServletHolder(new MetricsServlet(buffer)), path);
            server.setHandler(context);
            try {
                server.start();
                log.info("Started prometheus http server on port {}", port);
            } catch (Exception e) {
                log.error("Failed to start prometheus http server on port '{}'", port, e);
                throw new RuntimeException(e);
            }
        });
        runnerThread.setDaemon(false);
        runnerThread.start();
    }

    public void stop() throws Exception {
        log.info("Stopping prometheus http server");
        if (server.isRunning()) {
            server.stop();
            log.info("Stopped prometheus http server");
        }
    }

    public void addMetric(String name, MetricEvent event) {
        log.info("Adding metric '{}: {}' to prometheus http server", name, event);
        var q = buffer.computeIfAbsent(name, k -> new ConcurrentLinkedQueue<>());
        q.add(eventToPrometheusString(event));
    }

    private String eventToPrometheusString(MetricEvent event) {
        return String.format(PROMETHEUS_METRIC_TEMPLATE,
                event.name(),
                event.description(),
                event.name(),
                event.type(),
                event.name(),
                event.value()
        );
    }

}*/
