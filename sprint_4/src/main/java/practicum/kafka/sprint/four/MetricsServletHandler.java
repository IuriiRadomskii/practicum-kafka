package practicum.kafka.sprint.four;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MetricsServletHandler extends AbstractHandler {
    private final MetricsServlet servlet;
    private final String path;

    public MetricsServletHandler(MetricsServlet servlet, String path) {
        this.servlet = servlet;
        this.path = path;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (request.getRequestURI().equals(path)) {
            servlet.doGet(request, response);
            baseRequest.setHandled(true);
        }
    }
}
