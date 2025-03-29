package practicum.kafka.project.dto.shop;

import java.util.List;
import java.util.Map;

public record ProductInfo (
    String product_id,
    String name,
    String description,
    Price price,
    String category,
    String brand,
    Stock stock,
    String sku,
    List<String> tags,
    List<Image> images,
    Map<String, String> specifications,
    String created_at,
    String updated_at,
    String index,
    String store_id
) {}