package dev.ecommerce.service;

import dev.ecommerce.inventory.Inventory;
import dev.ecommerce.model.Order;
import dev.ecommerce.model.Order.OrderStatus;
import dev.ecommerce.tasks.BillGenerationTask;
import dev.ecommerce.tasks.OrderLogs;
import dev.ecommerce.tasks.Stats;
import dev.ecommerce.tasks.TaskEngine;

public class OrderService {
    private final Inventory inventory;
    private TaskEngine taskEngine;

    public OrderService(Inventory inventory, TaskEngine taskEngine) {
        this.inventory = inventory;
        this.taskEngine = taskEngine;
    }

    public void placeOrder(Order order) {
        Stats.incrementOrdersPlaced();
        order.setOrderStatus(OrderStatus.PROCESSING);

        boolean isAvailable = inventory.checkStore(order.getProduct().id(), order.getQuantity());
        if (!isAvailable) {
            order.setOrderStatus(OrderStatus.OUT_OF_STOCK);
            Stats.incrementOutOfStock();
            taskEngine.submit(new OrderLogs(order));
            return;
        }

        boolean isStoreUpdated = inventory.updateStore(order.getProduct().id(), order.getQuantity());
        if (!isStoreUpdated) {
            order.setOrderStatus(OrderStatus.ORDER_FAILED);
            Stats.incrementOrderFailed();
            taskEngine.submit(new OrderLogs(order));
            return;
        }

        String paymentStatus = PaymentService.processPayment();

        if ("failure".equalsIgnoreCase(paymentStatus)) {
            order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
            Stats.incrementPaymentFailed();
            taskEngine.submit(new OrderLogs(order));
            return;
        }

        order.setOrderStatus(OrderStatus.CONFIRMED);
        Stats.incrementOrdersConfirmed();
        taskEngine.submit(new OrderLogs(order));
        taskEngine.submit(new BillGenerationTask(order));
    }
}
