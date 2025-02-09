package practicum.kafka.sprint.four;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;

public class MetricsServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(MetricsServlet.class);

    private final Map<String, Queue<String>> buffer;

    public MetricsServlet(Map<String, Queue<String>> buffer) {
        this.buffer = buffer;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        buffer.values().forEach(q -> {
            var value = q.poll();
            if (value != null) {
                sb.append(value);
                sb.append("\n");
            }
        });
        var responseText = sb.toString();
        log.info("GET /metrics " + responseText);
        response.setContentType("text/plain");
        response.getWriter().write(responseText);
    }

}