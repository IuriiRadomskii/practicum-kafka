package practicum.kafka.sprint.four.connector;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MetricsServletHandler extends AbstractHandler {

    private static final Logger log = LoggerFactory.getLogger(MetricsServletHandler.class);

    private final MetricsServlet servlet;
    private final String path;

    public MetricsServletHandler(MetricsServlet servlet, String path) {
        this.servlet = servlet;
        this.path = path;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        log.info("Handling request {} {}", baseRequest.getMethod(), request.getRequestURI());
        if (request.getRequestURI().equals(path)) {
            servlet.doGet(request, response);
            baseRequest.setHandled(true);
        }
    }
}
