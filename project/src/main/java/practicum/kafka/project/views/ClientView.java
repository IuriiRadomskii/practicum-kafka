package practicum.kafka.project.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import practicum.kafka.project.services.ClientService;
import practicum.kafka.project.services.ShopService;

@Slf4j
@Route("")
public class ClientView extends VerticalLayout {

    private final ClientService clientService;
    private final ObjectMapper objectMapper;
    private final ShopService shopService;

    public ClientView(
            ClientService clientService,
            ShopService shopService
    ) {
        this.clientService = clientService;
        this.shopService = shopService;
        this.objectMapper = new ObjectMapper();
        setup();
    }

    public void setup() {
        Button shopButton = new Button("Start producing product");
        TextField nameField = new TextField("Name");
        Button searchButton = new Button("Find by name");
        Div displayPane1 = new Div();
        Button recommendationButton = new Button("Get recommendations");
        Div displayPane2 = new Div();

        shopButton.addClickListener(buttonClickEvent -> shopService.readAndSendProducts());

        searchButton.addClickListener(event -> {
            try {
                var productInfo = clientService.findByName(nameField.getValue());
                productInfo.ifPresentOrElse(p -> {
                    try {
                        displayPane1.setText(objectMapper.writeValueAsString(p));
                    } catch (Exception e) {
                        log.error("Error while fetching product", e);
                        displayPane1.setText(e.getMessage());
                    }
                }, () -> displayPane1.setText("Not found"));
            } catch (Exception e) {
                displayPane1.setText(e.getMessage());
            }
        });

        recommendationButton.addClickListener(event -> {
            try {
                var recommendations = clientService.getRecommendations();
                displayPane2.setText(objectMapper.writeValueAsString(recommendations));
            } catch (Exception e) {
                log.error("Error while fetching recommendations", e);
                displayPane1.setText(e.getMessage());
            }
        });

        add(shopButton, nameField, searchButton, displayPane1, recommendationButton, displayPane2);
    }
}