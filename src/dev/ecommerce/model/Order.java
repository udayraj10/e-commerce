package dev.ecommerce.model;

public class Order {

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PROCESSING,
        ORDER_FAILED,
        OUT_OF_STOCK,
        PAYMENT_FAILED
    }

    private int id;
    private Customer customer;
    private Product product;
    private int quantity;
    private OrderStatus orderStatus;

    public Order(int id, Customer customer, Product product, int quantity) {
        this.id = id;
        this.customer = customer;
        this.product = product;
        this.quantity = quantity;
        this.orderStatus = OrderStatus.PENDING;
    }

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
