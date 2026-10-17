package dev.ecommerce.tasks;

import dev.ecommerce.inventory.Inventory;
import dev.ecommerce.model.Product;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class FinalReport {
    private static final Path OUTPUT_DIR = Paths.get("output", "reports");
    private static final Path REPORT_FILE = OUTPUT_DIR.resolve("final-report.txt");

    public static void generateReport(Map<Integer, Product> products, Inventory inventory, int stockPerProduct) {
        try {
            if (!Files.exists(OUTPUT_DIR)) {
                Files.createDirectories(OUTPUT_DIR);
            }

            StringBuilder sb = new StringBuilder();

            sb.append("1. Order Summary\n");
            sb.append("--------------------------------------------------\n");
            sb.append(String.format("%-25s: %d%n", "Orders Placed", Stats.getOrdersPlaced()));
            sb.append(String.format("%-25s: %d%n", "Orders Confirmed", Stats.getOrdersConfirmed()));
            sb.append(String.format("%-25s: %d%n", "Orders Failed", Stats.getOrderFailed()));
            sb.append(String.format("%-25s: %d%n", "Payment Failed", Stats.getPaymentFailed()));
            sb.append(String.format("%-25s: %d%n", "Out of Stock", Stats.getOutOfStock()));
            sb.append(String.format("%-25s: %d%n", "Total Bills Generated", Stats.getBillCount()));
            sb.append("--------------------------------------------------\n\n");

            sb.append("2. Inventory Summary\n\n");
            sb.append(String.format("%-30s\t%-10s\t%-10s%n", "Product", "Sold", "Remaining"));

            Map<Integer, Integer> store = inventory.getStore();

            for (Product product : products.values()) {
                int remaining = (store != null) ? store.getOrDefault(product.id(), 0) : 0;
                int sold = stockPerProduct - remaining;

                sb.append(String.format("%-30s\t%-10d\t%-10d%n",
                        product.productName(),
                        sold,
                        remaining
                ));
            }

            Files.writeString(
                    REPORT_FILE,
                    sb.toString(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
