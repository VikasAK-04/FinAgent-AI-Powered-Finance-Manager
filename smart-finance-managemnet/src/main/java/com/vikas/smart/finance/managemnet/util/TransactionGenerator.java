package com.vikas.smart.finance.managemnet.util;

import com.vikas.smart.finance.managemnet.model.Transaction;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TransactionGenerator {

    private static final List<String> VENDORS = Arrays.asList(
            "Amazon", "Walmart", "Netflix", "Starbucks", "Spotify", "Uber", "Dominos", "Apple", "Google Play"
    );

    private static final List<String> CATEGORIES = Arrays.asList(
            "Shopping", "Bills", "Entertainment", "Food", "Transport", "Subscriptions", "Healthcare"
    );

    private static final Random RANDOM = new Random();

    /**
     * Generates a single random Transaction for a given user.
     *
     * @param userId ID of the user
     * @return Transaction object
     */
    public static Transaction generateTransaction(String userId) {
        Transaction t = new Transaction();
        t.setUserId(userId);
        t.setTransactionId(UUID.randomUUID().toString());
        t.setVendor(VENDORS.get(RANDOM.nextInt(VENDORS.size())));
        t.setCategory(CATEGORIES.get(RANDOM.nextInt(CATEGORIES.size())));
        t.setAmount(generateRandomAmount());
        t.setDate(generateRandomDate());
        return t;
    }

    /**
     * Generates a list of random transactions for a user.
     *
     * @param userId ID of the user
     * @param count  Number of transactions to generate
     * @return List of Transaction objects
     */
    public static List<Transaction> generateTransactions(String userId, int count) {
        return RANDOM.ints(count)
                .mapToObj(i -> generateTransaction(userId))
                .toList();
    }

    /**
     * Generates a random amount between $10 and $500 (2 decimal precision)
     */
    private static double generateRandomAmount() {
        double amount = 10 + (500 - 10) * RANDOM.nextDouble();
        return Math.round(amount * 100.0) / 100.0;
    }

    /**
     * Generates a random date within the last 30 days
     */
    private static LocalDate generateRandomDate() {
        int daysAgo = RANDOM.nextInt(30);
        return LocalDate.now().minusDays(daysAgo);
    }
}
