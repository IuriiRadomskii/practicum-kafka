package practicum.kafka.project.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class ConnectService {

    private static final String body = """
                {
                              "name": "file-stream-sink",
                              "connector.class": "org.apache.kafka.connect.file.FileStreamSinkConnector",
                              "tasks.max": "1",
                              "topics": "data-products-topic",
                              "file": "data-products-topic.out",
                              "key.converter": "org.apache.kafka.connect.storage.StringConverter",
                              "value.converter": "org.apache.kafka.connect.storage.StringConverter"
                            }
            """;

    public void runFileConnector() {
        try {
            URL url = new URL("http://localhost:8083/connectors/file-stream-sink/config");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("User-Agent", "IntelliJ HTTP Client/IntelliJ IDEA 2024.1.4");
            con.setRequestProperty("Accept", "*/*");
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            con.disconnect();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}

