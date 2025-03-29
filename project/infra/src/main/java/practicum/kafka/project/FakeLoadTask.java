package practicum.kafka.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import practicum.kafka.project.services.ClientService;
import practicum.kafka.project.services.ShopService;

@Slf4j
@RequiredArgsConstructor
public class FakeLoadTask implements CommandLineRunner {

    private final ShopService shopService;
    private final ClientService clientService;

    @Override
    public void run(String... args) throws Exception {
        //shopService.readAndSendProducts();
    }
}
