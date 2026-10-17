# Concurrent E-Commerce Order Processing System

## 1. Project Title
**Concurrent E-Commerce Order Processing System**

---

## 2. Project Overview
This project is a high-throughput, multi-threaded simulation of an e-commerce order processing system built in Java. It explores the practical challenges of concurrent programming when multiple customers attempt to purchase shared, limited inventory simultaneously. 

The application simulates thousands of concurrent customer requests, coordinating stock availability checks, atomic inventory deductions, simulated payment gateway transactions, asynchronous order logging, individualized bill generation, and aggregated reporting. It demonstrates how to maintain data integrity, eliminate race conditions, and optimize system responsiveness by decoupling business logic from I/O-intensive background tasks.

---

## 3. Key Features
- **Concurrent Order Processing**: Simulates high-concurrency order placement for thousands of customers utilizing managed thread pools.
- **Thread-Safe Inventory Management**: Protects shared product stock against race conditions and overselling using reentrant read-write locks.
- **Order Status Tracking**: Granular state management tracking every order through its lifecycle: `PENDING`, `PROCESSING`, `CONFIRMED`, `OUT_OF_STOCK`, `PAYMENT_FAILED`, and `ORDER_FAILED`.
- **Background Bill Generation & Order Logging**: Decouples disk I/O from the primary order execution flow by dispatching receipt creation and log writing to an asynchronous background task engine.
- **Final Inventory and Order Report**: Aggregates product inventory levels, units sold, remaining stock, and key order execution metrics into a clean summary report upon process completion.
- **Task Execution & Failure Tracking**: Thread-safe atomic counters maintain live system metrics across concurrent worker threads without locking overhead.

---

## 4. Technologies Used
- **Language**: Java 17+ (leveraging Java Records for immutable domain models `Customer` and `Product`)
- **Concurrency & Multithreading**:
  - `java.util.concurrent.ExecutorService` & `Executors` (Fixed thread pools for order workers and background tasks)
  - `java.util.concurrent.locks.ReentrantReadWriteLock` (Shared read locks for stock checks; exclusive write locks for stock updates)
  - `java.util.concurrent.atomic.AtomicInteger` (Non-blocking, lock-free atomic counters for system metrics)
  - `synchronized` blocks (Intrinsic locking for thread-safe sequential file writes)
  - `ThreadLocalRandom` (High-performance pseudo-random generation without thread contention)
- **I/O & File Systems**:
  - Java NIO (`java.nio.file.Files`, `Path`, `Paths`, `StandardOpenOption`, `SimpleFileVisitor`) for disk operations, directory cleanup, log streaming, and formatted invoice generation.

---

## 5. Architecture and Execution Flow

### System Components
1. **`Main`**: Orchestrator that wipes legacy output directories, seeds product inventory and customer data, initializes executors, distributes customer tasks, and coordinates graceful shutdowns.
2. **`OrderService`**: Core domain service handling order validation, inventory verification, simulated payment processing, order status transitions, and dispatching tasks to `TaskEngine`.
3. **`Inventory`**: Thread-safe shared resource that holds stock levels inside a `Map<Integer, Integer>`, protected by a `ReentrantReadWriteLock`.
4. **`TaskEngine`**: Dedicated asynchronous background worker pool managing I/O-heavy jobs without stalling main ordering threads.
5. **`BillGenerationTask`**: A `Runnable` background task that formats and generates individual bill `.txt` files in `output/bills/`.
6. **`OrderLogs`**: A `Runnable` background task providing thread-synchronized append operations to `output/logs/order-logs.txt`.
7. **`Stats`**: Centralized thread-safe telemetry store maintaining atomic counts for placed, confirmed, out-of-stock, and failed orders.
8. **`FinalReport`**: Generates a consolidated summary of order outcomes and remaining inventory balances in `output/reports/final-report.txt`.

### Architecture Diagram

```text
+-----------------------------------------------------------------------------------+
|                                       Main                                        |
|  - Cleans legacy output/ directory                                                |
|  - Seeds Products & Inventory (1000 units/prod)                                   |
|  - Generates Customers                                                            |
+-----------------------------------------+-----------------------------------------+
                                          |
                      Submits concurrent order tasks
                                          v
+-----------------------------------------------------------------------------------+
|                        Order Executor (ThreadPool)                                |
|                                                                                   |
|   Worker Thread 1      Worker Thread 2      Worker Thread 3 ... Worker Thread N   |
+-----------------------------------------+-----------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                                   OrderService                                    |
|                                                                                   |
|  1. Read Lock: checkStore(productId, quantity)  ----->  [ Inventory (Shared) ]    |
|  2. Write Lock: updateStore(productId, quantity) ----->  [ ReentrantReadWriteLock] |
|  3. Payment Processing                           ----->  [ PaymentService (90/10) ]|
|  4. Telemetry Tracking                           ----->  [ Stats (AtomicInteger)  ]|
+-----------------------------------------+-----------------------------------------+
                                          |
                      Dispatches background I/O tasks
                                          v
+-----------------------------------------------------------------------------------+
|                            TaskEngine (ThreadPool)                                |
|                                                                                   |
|     +----------------------------+             +----------------------------+     |
|     |     BillGenerationTask     |             |         OrderLogs          |     |
|     |  (Independent File I/O)    |             |    (Thread-Safe Lock)      |     |
|     +--------------+-------------+             +--------------+-------------+     |
|                    |                                          |                   |
|                    v                                          v                   |
|      output/bills/BILL_*.txt                       output/logs/order-logs.txt     |
+-----------------------------------------+-----------------------------------------+
                                          |
                  Both Executors Terminated via awaitTermination()
                                          v
+-----------------------------------------------------------------------------------+
|                                   FinalReport                                     |
|  - Reads remaining stock from Inventory                                           |
|  - Reads atomic statistics from Stats                                             |
|  - Writes consolidated summary to output/reports/final-report.txt                  |
+-----------------------------------------------------------------------------------+
```

### Execution Step-by-Step Flow
1. **Startup & Cleanup**: `Main` executes `deleteOutputFolderIfExists()` to wipe previous artifacts.
2. **Data Seeding**: Seeds product catalogs and initializes stock levels (e.g., 1,200 units per product) in `Inventory`, followed by generating customer records.
3. **Concurrent Submission**: Customer orders are submitted as concurrent tasks to the primary `orderExecutor`.
4. **Stock Check**: `OrderService` acquires `readLock` to verify if inventory satisfies the requested quantity. If insufficient, marks `OUT_OF_STOCK`, updates stats, logs, and exits.
5. **Stock Deduction**: `OrderService` acquires `writeLock` to deduct inventory. If deduction fails, marks `ORDER_FAILED`, updates stats, logs, and exits.
6. **Payment Execution**: `PaymentService.processPayment()` simulates payment gateway response (90% success, 10% failure). If payment fails, marks `PAYMENT_FAILED`, updates stats, logs, and exits.
7. **Confirmation & Offloading**: Upon successful payment, order is set to `CONFIRMED`. `OrderService` submits an `OrderLogs` task and a `BillGenerationTask` to the `TaskEngine`.
8. **Graceful Shutdown**:
   - `orderExecutor.shutdown()` followed by `waitForTermination()` ensures all orders finish processing.
   - `taskEngine.shutdown()` followed by `taskEngine.awaitTermination()` guarantees all background log files and customer bills are completely written to disk.
9. **Final Reporting**: `FinalReport.generateReport()` reads final stock and atomic telemetry from `Stats`, outputting the summary to disk.

---

## 6. Concurrency Concepts Learned
- **Race Conditions**: Encountered when multiple threads attempt to check and deduct the same product inventory concurrently. Without locking, threads read stale stock counts, leading to overselling.
- **Shared-State Protection**: Implemented `ReentrantReadWriteLock` to permit concurrent readers while giving exclusive access to modifying threads, avoiding write-write and read-write collisions.
- **Lock-Free Atomic Operations**: Utilized `AtomicInteger` in `Stats` for telemetry counters (`incrementAndGet()`, `get()`), avoiding synchronization bottlenecks on high-frequency metrics.
- **Intrinsic Locking (`synchronized`)**: Applied an explicit object lock in `OrderLogs` to guarantee sequential, uncorrupted append operations when multiple background threads write to a single file.
- **Thread Lifecycle & Coordination**: Mastered phased executor terminations using `.shutdown()` and `.awaitTermination()`. Preventing dangling worker threads ensures the JVM terminates cleanly only after all background I/O operations conclude.
- **Decoupled Workloads**: Separating CPU-bound business rules (order placement) from I/O-bound tasks (file writing) prevents worker thread starvation and improves overall responsiveness.

---

## 7. Inventory Safety
To guarantee that products are never oversold under high concurrency:
1. **Lock Separation**:
   - `checkStore(productId, quantity)` acquires `readLock`: Allows multiple concurrent threads to read available stock simultaneously without blocking each other.
   - `updateStore(productId, quantity)` acquires `writeLock`: Provides exclusive mutual exclusion. No other thread can read or write while stock is being deducted.
2. **Defensive Validation inside the Write Lock**:
   Even if an order passes `checkStore`, other threads could deduct stock before this thread acquires the `writeLock`. Inside `updateStore`, the stock is checked again:
   ```java
   writeLock.lock();
   try {
       int currentStock = store.getOrDefault(productId, 0);
       if (currentStock >= quantity) {
           store.put(productId, currentStock - quantity);
           return true;
       }
       return false;
   } finally {
       writeLock.unlock();
   }
   ```
   This prevents overselling and guarantees that inventory balances remain strictly non-negative.

---

## 8. Background Task Management
- **Asynchronous Execution**: Logging and bill generation are computationally independent of the order fulfillment loop. Offloading them to `TaskEngine` ensures order threads return quickly.
- **Independent Bill Generation**: Each `BillGenerationTask` writes to an isolated file (`BILL_<orderId>.txt`), allowing worker threads to write bills simultaneously without thread contention or locks.
- **Thread-Safe Shared Logging**: `OrderLogs` shares a single destination file (`output/logs/order-logs.txt`). File writes are wrapped in `synchronized (LOCK)` using `StandardOpenOption.APPEND` to avoid inter-thread interleaved log corruption.
- **Barrier Synchronization Before Reporting**:
  To prevent the final report from printing incomplete numbers, `Main` applies a sequential shutdown barrier:
  ```java
  orderExecutor.shutdown();
  waitForTermination(orderExecutor); // Wait for all orders to process
  taskEngine.shutdown();
  taskEngine.awaitTermination();     // Wait for all bills and logs to finish writing
  FinalReport.generateReport(...);    // Generate report with 100% completed stats
  ```

---

## 9. Benchmark Results

### Benchmark Summary
> **Benchmarked different thread-pool configurations for order processing and background task execution. Results varied across repeated runs, demonstrating that increasing thread counts does not guarantee better performance. Execution time depends on thread scheduling, workload characteristics, resource contention, and I/O.**

### Key Observations
- **Thread Count vs. Core Saturation**: Increasing thread counts beyond available hardware CPU cores introduced context-switching overhead and thread scheduling latency rather than linear throughput gains.
- **I/O Contention**: While concurrent invoice writing benefited from multiple threads, shared disk access and lock synchronization on the central log file became the primary throughput bottleneck under heavy thread concurrency.
- **Lock Contention**: When numerous threads targeted overlapping product IDs, acquisition wait times on the inventory write lock increased, illustrating the trade-off between strict safety guarantees and raw parallelism.
