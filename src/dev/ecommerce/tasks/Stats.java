package dev.ecommerce.tasks;

import java.util.concurrent.atomic.AtomicInteger;

public class Stats {
    private static final AtomicInteger billCount = new AtomicInteger(0);
    private static final AtomicInteger ordersPlaced = new AtomicInteger(0);
    private static final AtomicInteger ordersConfirmed = new AtomicInteger(0);
    private static final AtomicInteger orderFailed = new AtomicInteger(0);
    private static final AtomicInteger paymentFailed = new AtomicInteger(0);
    private static final AtomicInteger outOfStock = new AtomicInteger(0);

    public static void incrementBill() {
        billCount.incrementAndGet();
    }

    public static int getBillCount() {
        return billCount.get();
    }

    public static void incrementOrdersPlaced() {
        ordersPlaced.incrementAndGet();
    }

    public static int getOrdersPlaced() {
        return ordersPlaced.get();
    }

    public static void incrementOrdersConfirmed() {
        ordersConfirmed.incrementAndGet();
    }

    public static int getOrdersConfirmed() {
        return ordersConfirmed.get();
    }

    public static void incrementOrderFailed() {
        orderFailed.incrementAndGet();
    }

    public static int getOrderFailed() {
        return orderFailed.get();
    }

    public static void incrementPaymentFailed() {
        paymentFailed.incrementAndGet();
    }

    public static int getPaymentFailed() {
        return paymentFailed.get();
    }

    public static void incrementOutOfStock() {
        outOfStock.incrementAndGet();
    }

    public static int getOutOfStock() {
        return outOfStock.get();
    }
}
