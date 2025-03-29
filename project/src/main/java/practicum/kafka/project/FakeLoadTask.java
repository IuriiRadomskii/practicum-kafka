package practicum.kafka.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import practicum.kafka.project.services.ProductFilterService;
import practicum.kafka.project.services.ShopService;

@Slf4j
@Component
@RequiredArgsConstructor
public class FakeLoadTask implements CommandLineRunner {

    private final ShopService shopService;
    private final ProductFilterService productFilterService;

    @Override
    public void run(String... args) throws Exception {
        productFilterService.process();
        shopService.readAndSendProducts();
    }

}
