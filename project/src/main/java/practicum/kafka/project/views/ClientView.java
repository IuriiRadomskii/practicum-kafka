package practicum.kafka.project.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import practicum.kafka.project.services.ClientService;
import practicum.kafka.project.services.ConnectService;
import practicum.kafka.project.services.HdfsTransferService;
import practicum.kafka.project.services.ShopService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Route("")
public class ClientView extends VerticalLayout {

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final ClientService clientService;
    private final ObjectMapper objectMapper;
    private final ShopService shopService;
    private final HdfsTransferService hdfsTransferService;
    private final ConnectService connectService;

    public ClientView(
            ClientService clientService,
            ShopService shopService,
            HdfsTransferService hdfsTransferService,
            ConnectService connectService
    ) {
        this.clientService = clientService;
        this.shopService = shopService;
        this.hdfsTransferService = hdfsTransferService;
        this.objectMapper = new ObjectMapper();
        this.connectService = connectService;
        setup();
    }

    public void setup() {
        Button shopButton = new Button("Start producing product");
        Button connectButton = new Button("Run file connector");
        Button hdfsButton = new Button("Start transferring client requests to HDFS");
        TextField nameField = new TextField("Name");
        Button searchButton = new Button("Find by name");
        Div displayPane1 = new Div();
        Button recommendationButton = new Button("Get recommendations");
        Div displayPane2 = new Div();

        shopButton.addClickListener(buttonClickEvent -> executorService.submit(shopService::readAndSendProducts));
        connectButton.addClickListener(buttonClickEvent -> executorService.submit(connectService::runFileConnector));
        hdfsButton.addClickListener(buttonClickEvent -> executorService.submit(hdfsTransferService::transferDataToHdfs));

        searchButton.addClickListener(event -> {
            try {
                var productInfo = clientService.findByName(nameField.getValue());
                if (productInfo.isEmpty()) {
                    displayPane1.setText(nameField + "'s were not found");
                }
                try {
                    displayPane1.setText(objectMapper.writeValueAsString(productInfo));
                } catch (Exception e) {
                    log.error("Error while fetching product", e);
                    displayPane1.setText(e.getMessage());
                }
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

        add(shopButton, connectButton, hdfsButton, nameField, searchButton, displayPane1, recommendationButton, displayPane2);
    }
}