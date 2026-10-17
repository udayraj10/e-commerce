package dev.ecommerce.tasks;

import dev.ecommerce.model.Customer;
import dev.ecommerce.model.Order;
import dev.ecommerce.model.Product;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

public class OrderLogs implements Runnable {
    private static final Path LOG_DIR = Paths.get("output", "logs");
    private static final Path LOG_FILE = LOG_DIR.resolve("order-logs.txt");
    private static final Object LOCK = new Object();

    private final Order order;

    public OrderLogs(Order order) {
        this.order = order;
    }

    @Override
    public void run() {
        if (order == null) {
            return;
        }

        Customer customer = order.getCustomer();
        Product product = order.getProduct();

        String orderIdStr = "ORD-" + order.getId();
        String customerName = customer != null ? customer.customerName() : "Unknown";
        String productName = product != null ? product.productName() : "Unknown";
        int qty = order.getQuantity();
        double price = product != null ? product.price() : 0.0;
        double total = qty * price;
        String status = order.getOrderStatus() != null ? order.getOrderStatus().name() : "UNKNOWN";

        String itemQty = productName + " x " + qty;

        String logLine = String.format(Locale.US,
                "Order %-10s | %-12s | %-30s | Total: %-10.2f | Status: %s%n",
                orderIdStr,
                customerName,
                itemQty,
                total,
                status
        );

        synchronized (LOCK) {
            try {
                if (!Files.exists(LOG_DIR)) {
                    Files.createDirectories(LOG_DIR);
                }
                Files.writeString(
                        LOG_FILE,
                        logLine,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.APPEND
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
