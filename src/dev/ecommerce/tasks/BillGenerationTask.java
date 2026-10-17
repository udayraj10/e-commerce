package dev.ecommerce.tasks;

import dev.ecommerce.model.Customer;
import dev.ecommerce.model.Order;
import dev.ecommerce.model.Product;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class BillGenerationTask implements Runnable {
    private static final Path OUTPUT_DIR = Paths.get("output", "bills");

    private final Order order;

    public BillGenerationTask(Order order) {
        this.order = order;
    }

    @Override
    public void run() {
        try {
            if (!Files.exists(OUTPUT_DIR)) {
                Files.createDirectories(OUTPUT_DIR);
            }

            Path billFile = OUTPUT_DIR.resolve("BILL_" + order.getId() + ".txt");

            Customer customer = order.getCustomer();
            Product product = order.getProduct();

            String customerName = customer.customerName();
            String productName = product.productName();

            LocalDateTime now = LocalDateTime.now();
            String dateStr = now.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH));
            String timeStr = now.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH));

            int qty = order.getQuantity();
            double total = qty * product.price();

            String invoiceNo = String.format("INV-%d-%d", now.getYear(), order.getId());

            StringBuilder sb = new StringBuilder();
            sb.append("==================================================\n");
            sb.append("                  SUPER STORE                     \n");
            sb.append("           123 Main Street, Bengaluru             \n");
            sb.append("             Phone: +1 234 567 890                \n");
            sb.append("--------------------------------------------------\n");
            sb.append(String.format("Invoice No: %-17sDate: %-15s\n", invoiceNo, dateStr));
            sb.append(String.format("Customer  : %-17sTime: %-15s\n", customerName, timeStr));
            sb.append("--------------------------------------------------\n");
            sb.append("Item Name             Qty      Price        Total \n");
            sb.append("--------------------------------------------------\n");
            sb.append(String.format(Locale.US, "%-20s %3d %10.2f %12.2f \n",
                    productName.length() > 20 ? productName.substring(0, 20) : productName,
                    qty,
                    product.price(),
                    total));
            sb.append("--------------------------------------------------\n");
            sb.append(String.format(Locale.US, "Grand Total:                         ₹%10.2f \n", total));
            sb.append("==================================================\n");
            sb.append("          Thank you for shopping with us!         \n");
            sb.append("==================================================\n");

            Files.writeString(billFile, sb.toString());
            Stats.incrementBill();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
