package dev.ecommerce.inventory;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Inventory {
    private Map<Integer, Integer> store;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public Inventory() {}

    public boolean checkStore(int productId, int quantity) {
        readLock.lock();
        try {
            Integer stock = store.getOrDefault(productId, 0);
            if (stock <= quantity) {
                return false;
            }
            return true;
        } finally {
            readLock.unlock();
        }
    }

    public boolean updateStore(int productId, int quantity) {
        writeLock.lock();
        try {
            if (store == null) {
                return false;
            }
            int currentStock = store.getOrDefault(productId, 0);
            if (currentStock >= quantity) {
                store.put(productId, currentStock - quantity);
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    public Map<Integer, Integer> getStore() {
        return store;
    }

    public void setStore(Map<Integer, Integer> store) {
        this.store = store;
    }
}
