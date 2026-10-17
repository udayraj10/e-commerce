package dev.ecommerce.seeder;

import dev.ecommerce.inventory.Inventory;
import dev.ecommerce.model.Customer;
import dev.ecommerce.model.Product;
import dev.ecommerce.util.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSeeder {

    public static List<Customer> seedCustomers(int capacity) {
        List<Customer> customers = new ArrayList<>(capacity);
        for (int i = 1; i <= capacity; i++) {
            int customerId = Helper.generateId();
            String customerName = "customer" + i;
            customers.add(new Customer(customerId, customerName));
        }
        return customers;
    }

    public static void seedInventory(Map<Integer, Product> products, Inventory inventory, int stockPerProduct) {
        Map<Integer, Integer> store = inventory.getStore();
        if (store == null) {
            store = new HashMap<>();
            inventory.setStore(store);
        }

        List<Product> productList = Helper.getProducts();
        for (Product product : productList) {
            products.put(product.id(), product);
            store.put(product.id(), stockPerProduct);
        }
    }
}
