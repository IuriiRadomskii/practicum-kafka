package practicum.kafka.project.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import practicum.kafka.project.services.ClientService;

@Slf4j
@Route("")
public class ClientView extends VerticalLayout {

    private final ClientService clientService;
    private final ObjectMapper objectMapper;

    public ClientView(ClientService clientService) {
        this.clientService = clientService;
        this.objectMapper = new ObjectMapper();
        setup();
    }

    public void setup() {
        TextField nameField = new TextField("Name");
        Button searchButton = new Button("Find by name");
        Div displayPane1 = new Div();
        Button recommendationButton = new Button("Get recommendations");
        Div displayPane2 = new Div();
        searchButton.addClickListener(event -> {
            try {
                var productInfo = clientService.findByName(nameField.getValue());
                displayPane1.setText(objectMapper.writeValueAsString(productInfo));
            } catch (Exception e) {
                log.error("Error while fetching product", e);
            }
        });
        recommendationButton.addClickListener(event -> {
            try {
                var recommendations = clientService.getRecommendations();
                displayPane2.setText(objectMapper.writeValueAsString(recommendations));
            } catch (Exception e) {
                log.error("Error while fetching recommendations", e);
            }
        });
        add(nameField, searchButton, displayPane1, recommendationButton, displayPane2);
    }
}