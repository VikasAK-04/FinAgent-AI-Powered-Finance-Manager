package com.vikas.smart.finance.managemnet.service;

import com.vikas.smart.finance.managemnet.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

@Service
public class TransactionMonitorService {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private InsightService insightService;

    // Configuration
    private final int MAX_TRANSACTIONS_PER_USER = 20; // Limit to 20 transactions per user
    private final List<String> userIds = Arrays.asList("user1", "user2", "user3");
    private final Random random = new Random();
    private final List<String> vendors = Arrays.asList("Amazon", "Walmart", "Netflix", "Starbucks", "Spotify", "Uber", "Apple");
    private final List<String> categories = Arrays.asList("Shopping", "Bills", "Entertainment", "Food", "Transport");

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void startMonitoring() {
        System.out.println("🚀 Starting Transaction Monitor Service...");
        System.out.println("📊 Max transactions per user: " + MAX_TRANSACTIONS_PER_USER);

        // Generate initial transactions for all users
        for (String userId : userIds) {
            generateInitialTransactions(userId);
        }

        // Schedule periodic transaction generation (every 2 minutes instead of 1)
        scheduler.scheduleAtFixedRate(this::runSimulation, 2, 2, TimeUnit.MINUTES);
    }

    /**
     * Generate initial set of transactions for a new user
     */
    private void generateInitialTransactions(String userId) {
        List<Transaction> existing = transactionService.getTransactions(userId);
        if (existing.isEmpty()) {
            System.out.println("📝 Generating initial transactions for: " + userId);
            for (int i = 0; i < 10; i++) { // Start with 10 transactions
                Transaction t = generateTransaction(userId, LocalDate.now().minusDays(random.nextInt(30)));
                transactionService.saveTransaction(t);
            }
        }
    }

    /**
     * Periodic simulation - adds 1 transaction per user if under limit
     */
    private void runSimulation() {
        for (String userId : userIds) {
            List<Transaction> existing = transactionService.getTransactions(userId);

            if (existing.size() < MAX_TRANSACTIONS_PER_USER) {
                // Add one new transaction
                Transaction t = generateTransaction(userId, LocalDate.now());
                transactionService.saveTransaction(t);
                System.out.println("✅ Generated transaction for " + userId +
                        " | Vendor: " + t.getVendor() +
                        " | Amount: $" + t.getAmount() +
                        " | Total: " + (existing.size() + 1) + "/" + MAX_TRANSACTIONS_PER_USER);
            } else {
                // Delete oldest transaction and add new one (rolling window)
                deleteOldestTransaction(userId, existing);
                Transaction t = generateTransaction(userId, LocalDate.now());
                transactionService.saveTransaction(t);
                System.out.println("🔄 Replaced oldest transaction for " + userId +
                        " | Vendor: " + t.getVendor() +
                        " | Amount: $" + t.getAmount());
            }
        }
    }

    /**
     * Delete the oldest transaction to maintain limit
     */
    private void deleteOldestTransaction(String userId, List<Transaction> transactions) {
        if (transactions.isEmpty()) return;

        // Find oldest by date
        Transaction oldest = transactions.stream()
                .min(Comparator.comparing(Transaction::getDate))
                .orElse(transactions.get(0));

        transactionService.deleteTransaction(userId, oldest.getTransactionId());
        System.out.println("🗑️ Deleted oldest transaction: " + oldest.getTransactionId());
    }

    /**
     * Generate a single transaction with specific date
     */
    private Transaction generateTransaction(String userId, LocalDate date) {
        Transaction t = new Transaction();
        t.setUserId(userId);
        t.setTransactionId(UUID.randomUUID().toString());
        t.setVendor(vendors.get(random.nextInt(vendors.size())));
        t.setCategory(categories.get(random.nextInt(categories.size())));
        t.setAmount(Math.round((10 + random.nextDouble() * 490) * 100.0) / 100.0); // $10-$500
        t.setDate(date);
        return t;
    }

    @PreDestroy
    public void shutdown() {
        System.out.println("🛑 Shutting down Transaction Monitor Service...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}