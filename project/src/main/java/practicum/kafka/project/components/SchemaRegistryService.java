package practicum.kafka.project.components;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaRegistryService {

    private final SchemaRegistryClient client;

    public String loadSchema(String name) {
        ClassPathResource classPathResource = new ClassPathResource("schemas/" + name);
        try (InputStreamReader reader = new InputStreamReader(classPathResource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            log.warn("Failed to load schema '{}'", name, e);
            return null;
        }
    }

    public void registerSchema(String topic, String schemaString) {
        ParsedSchema schema = new JsonSchema(schemaString);
        try {
            client.register(topic, schema);
            log.info("Schema registered for {}", topic);
        } catch (IOException | RestClientException e) {
            log.error("Failed to register schema: {}", e.getMessage());
        }
    }
}
