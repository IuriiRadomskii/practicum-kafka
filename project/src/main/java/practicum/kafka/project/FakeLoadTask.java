package practicum.kafka.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import practicum.kafka.project.services.HdfsTransferService;

@Slf4j
@Component
@RequiredArgsConstructor
public class FakeLoadTask implements CommandLineRunner {

    //private final ShopService shopService;
    //private final ProductFilterService productFilterService;
    private final HdfsTransferService hdfsService;
    //private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Override
    public void run(String... args) throws Exception {
        hdfsService.transferDataToHdfs();
    }

}
