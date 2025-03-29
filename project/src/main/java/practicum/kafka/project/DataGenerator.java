package practicum.kafka.project;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DataGenerator {

    static List<String> HOUSEHOLD_APPLIANCES = List.of("Household appliances", "Fridge", "TV", "Washing machine", "Dishwasher", "Vacuum cleaner", "Owen", "Toaster", "Blender");
    static List<String> IOT_DEVICES = List.of("IoT devices", "Smartbulb", "Smartswitch", "Smart Thermostat", "Smart Door Lock", "Smart Home Camera", "Smart Speaker", "Smart Display", "Smart Smoke Detector", "Smart Plug", "Smart Vacuum Cleaner");    static List<String> BOOKS = List.of("The Silent Ocean", "Moonlight Shadows", "Dreams in Time", "The Last Dreamwalker", "Echoes of the Past", "Whispers of the Desert", "Winter's Ghost", "Song of the Storm", "Dance of The Dragonfly", "Beneath the Pines");    static List<String> ELECTRONIC_DEVICES = List.of("Laptop", "Smartphone", "Tablet", "Headphones", "Camera", "Speaker", "Monitor", "Keyboard", "Mouse", "Printer");
    static List<String> FOOD_ITEMS = List.of("Foods", "Bread", "Milk", "Cheese", "Eggs", "Butter", "Orange Juice", "Cereal", "Rice", "Chicken", "Beef");
    static List<String> OFFICE_SUPPLIES = List.of("Office supplies", "Pen", "Pencil", "Eraser", "Notebook", "Marker", "Stapler", "Paper Clips", "File Folders", "Scissors", "Tape");
    static List<List<String>> products = List.of(HOUSEHOLD_APPLIANCES, IOT_DEVICES, FOOD_ITEMS, OFFICE_SUPPLIES);
    static List<String> COMPANIES = List.of("XYZ", "Fuguzzy", "Thrilled Company", "PIPI", "Yandex");

    public static void main(String[] args) throws Exception {
        Path resources = Path.of(System.getProperty("user.dir"), "project", "src", "main", "resources");
        Path template = resources.resolve("product-template.json");
        Path outDir = resources.resolve("products");
        String templateProduct = Files.readString(template);
        var random = new Random();
        for (int i = 1; i <= 1000; i++) {
            String filename = "product-" + i + ".json";
            var nameCompanyTag = rnd();
            System.out.println(Arrays.asList(nameCompanyTag));
            String fileContent = String.format(templateProduct,
                    i,
                    nameCompanyTag[0],
                    nameCompanyTag[1],
                    nameCompanyTag[0],
                    nameCompanyTag[1],
                    random.nextInt(1000, 10000),
                    nameCompanyTag[2],
                    nameCompanyTag[1],
                    random.nextInt(100, 200),
                    random.nextInt(0, 100),
                    nameCompanyTag[1],
                    nameCompanyTag[2]
            );
            Files.write(outDir.resolve(filename), fileContent.getBytes());
        }
    }

    static String[] rnd() {
        var nameCompanyTag = new String[3];
        var random = new Random();
        var list = products.get(random.nextInt(products.size()));
        var name = list.get(random.nextInt(1, list.size()));
        nameCompanyTag[0] = name;
        nameCompanyTag[1] = COMPANIES.get(random.nextInt(COMPANIES.size())) + random.nextInt(0, 10);
        nameCompanyTag[2] = list.get(0);
        return nameCompanyTag;
    }

}
