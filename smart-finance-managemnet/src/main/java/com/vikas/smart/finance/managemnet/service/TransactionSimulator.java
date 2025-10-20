package com.vikas.smart.finance.managemnet.service;

import com.vikas.smart.finance.managemnet.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class TransactionSimulator {

    private static final Logger logger = Logger.getLogger(TransactionSimulator.class.getName());

    @Autowired
    private TransactionService transactionService;

    private final Random random = new Random();

    private final List<String> vendors = List.of("Amazon", "Walmart", "Netflix", "Starbucks", "Spotify");
    private final List<String> categories = List.of("Shopping", "Bills", "Entertainment", "Food", "Transport");

    /** Generate random transactions for a single user asynchronously */
    @Async
    public void generateForUser(String userId, int count) {
        for (int i = 0; i < count; i++) {
            Transaction t = createRandomTransaction(userId);
            transactionService.saveTransaction(t);
            logger.info("Generated transaction: " + t.getTransactionId() +
                    " | User: " + userId + " | Vendor: " + t.getVendor() + " | Amount: " + t.getAmount());
        }
    }

    /** Generate for all users asynchronously */
    @Async
    public void generateForAllUsers(List<String> userIds, int countPerUser) {
        for (String userId : userIds) {
            generateForUser(userId, countPerUser);
        }
    }

    /** Helper method to create a random transaction */
    private Transaction createRandomTransaction(String userId) {
        Transaction t = new Transaction();
        t.setUserId(userId);
        t.setTransactionId(UUID.randomUUID().toString());
        t.setVendor(vendors.get(random.nextInt(vendors.size())));
        t.setCategory(categories.get(random.nextInt(categories.size())));
        t.setAmount(Math.round((10 + random.nextDouble() * 490) * 100.0) / 100.0); // 10-500
        t.setDate(LocalDate.now().minusDays(random.nextInt(30))); // last 30 days
        return t;
    }
}
