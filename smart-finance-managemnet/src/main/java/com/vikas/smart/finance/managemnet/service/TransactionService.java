package com.vikas.smart.finance.managemnet.service;

import com.vikas.smart.finance.managemnet.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final DynamoDbTable<Transaction> transactionTable;

    @Autowired
    @Lazy
    private InsightService insightService;

    @Autowired
    public TransactionService(DynamoDbEnhancedClient enhancedClient) {
        this.transactionTable = enhancedClient.table("Transaction", TableSchema.fromBean(Transaction.class));
    }

    /**
     * Save a transaction and trigger insight generation asynchronously
     */
    public void saveTransaction(Transaction transaction) {
        transactionTable.putItem(transaction);
        generateInsightsAsync(transaction.getUserId());
    }

    /**
     * Delete a specific transaction
     */
    public void deleteTransaction(String userId, String transactionId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue(transactionId)
                .build();
        transactionTable.deleteItem(key);
    }

    /**
     * Delete all transactions for a user
     */
    public void deleteAllTransactions(String userId) {
        List<Transaction> transactions = getTransactions(userId);
        for (Transaction t : transactions) {
            deleteTransaction(userId, t.getTransactionId());
        }
    }

    /**
     * Async insight generation to avoid blocking the main thread
     */
    @Async
    public void generateInsightsAsync(String userId) {
        if (insightService != null) {
            insightService.generateInsights(userId);
        }
    }

    /**
     * Get all transactions for a user
     */
    public List<Transaction> getTransactions(String userId) {
        List<Transaction> transactions = new ArrayList<>();
        transactionTable.query(QueryConditional.keyEqualTo(k -> k.partitionValue(userId)))
                .items()
                .forEach(transactions::add);
        return transactions;
    }

    /**
     * Get monthly spending summary per category
     */
    public Map<String, Double> getMonthlySummary(String userId, int month, int year) {
        List<Transaction> transactions = getTransactions(userId);

        return transactions.stream()
                .filter(t -> {
                    LocalDate date = t.getDate();
                    return date.getMonthValue() == month && date.getYear() == year;
                })
                .collect(Collectors.groupingBy(Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)));
    }
}