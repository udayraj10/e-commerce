package dev.ecommerce.service;

public class PaymentService {

    public static String processPayment() {
        boolean isSuccess = Math.random() < 0.9;

        return isSuccess ? "success" : "failure";
    }
}
