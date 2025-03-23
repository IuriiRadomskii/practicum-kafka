package practicum.kafka.project.dto.shop;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProductInfo {
    private String product_id;
    private String name;
    private String description;
    private Price price;
    private String category;
    private String brand;
    private Stock stock;
    private String sku;
    private List<String> tags;
    private List<Image> images;
    private Map<String, String> specifications;
    private String created_at;
    private String updated_at;
    private String index;
    private String store_id;
}