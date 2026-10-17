package dev.ecommerce.util;

import dev.ecommerce.model.Product;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Helper {

    public static int generateId() {
        int timePart = (LocalTime.now().getNano() / 10_000_000) % 100;
        int randomPart = ThreadLocalRandom.current().nextInt(100, 1000);

        return (timePart * 1000) + randomPart;
    }

    public static List<Product> getProducts() {
        return List.of(
                new Product(generateId(), "Laptop", 999.99),
                new Product(generateId(), "Smartphone", 699.99),
                new Product(generateId(), "Wireless Headphones", 149.99),
                new Product(generateId(), "Smartwatch", 199.99),
                new Product(generateId(), "Mechanical Keyboard", 89.99),
                new Product(generateId(), "Gaming Mouse", 49.99),
                new Product(generateId(), "27-inch Monitor", 249.99),
                new Product(generateId(), "USB-C Hub", 39.99),
                new Product(generateId(), "External SSD 1TB", 119.99),
                new Product(generateId(), "Bluetooth Speaker", 59.99),
                new Product(generateId(), "Webcam 1080p", 79.99),
                new Product(generateId(), "Desk Mat", 19.99),
                new Product(generateId(), "Noise-Cancelling Earbuds", 129.99),
                new Product(generateId(), "Tablet 10-inch", 349.99),
                new Product(generateId(), "Graphics Drawing Tablet", 159.99),
                new Product(generateId(), "Ergonomic Office Chair", 299.99),
                new Product(generateId(), "Adjustable Desk Lamp", 34.99),
                new Product(generateId(), "Power Bank 20000mAh", 45.99),
                new Product(generateId(), "Wi-Fi 6 Router", 109.99),
                new Product(generateId(), "Microphone with Arm", 84.99)
        );
    }
}
