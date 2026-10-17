package dev.ecommerce;

import dev.ecommerce.inventory.Inventory;
import dev.ecommerce.model.Customer;
import dev.ecommerce.model.Order;
import dev.ecommerce.model.Product;
import dev.ecommerce.seeder.DataSeeder;
import dev.ecommerce.service.OrderService;
import dev.ecommerce.tasks.FinalReport;
import dev.ecommerce.tasks.TaskEngine;
import dev.ecommerce.util.Helper;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        deleteOutputFolderIfExists();

        TaskEngine taskEngine = new TaskEngine();

        final int INITIAL_STOCK_PER_PRODUCT = 1200;
        Map<Integer, Product> products = new HashMap<>();
        Inventory inventory = new Inventory();
        DataSeeder.seedInventory(products, inventory, INITIAL_STOCK_PER_PRODUCT);
        List<Customer> customers = DataSeeder.seedCustomers(10000);
        OrderService orderService = new OrderService(inventory, taskEngine);
        List<Product> productList = new ArrayList<>(products.values());

        ExecutorService orderExecutor = Executors.newFixedThreadPool(5);

        try {
            for (Customer c : customers) {
                orderExecutor.submit(() -> {
                    Product randomProduct = productList.get(ThreadLocalRandom.current().nextInt(productList.size()));
                    int randomQuantity = ThreadLocalRandom.current().nextInt(1, 5);

                    Order order = new Order(
                            Helper.generateId(),
                            c,
                            randomProduct,
                            randomQuantity
                    );

                    orderService.placeOrder(order);
                });
            }
        }
        finally {
            orderExecutor.shutdown();
            waitForTermination(orderExecutor);
            taskEngine.shutdown();
            taskEngine.awaitTermination();
        }

        FinalReport.generateReport(products, inventory, INITIAL_STOCK_PER_PRODUCT);
    }

    private static void deleteOutputFolderIfExists() {
        Path outputPath = Paths.get("output");
        if (Files.exists(outputPath)) {
            try {
                Files.walkFileTree(outputPath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.deleteIfExists(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void waitForTermination(ExecutorService executor) {
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
