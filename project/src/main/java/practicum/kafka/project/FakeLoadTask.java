package practicum.kafka.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import practicum.kafka.project.services.ProductFilterService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FakeLoadTask implements CommandLineRunner {

    private final ProductFilterService filter;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void run(String... args) throws Exception {
        executor.submit(filter::process);
    }

}
