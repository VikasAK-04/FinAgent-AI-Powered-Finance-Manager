package com.vikas.smart.finance.managemnet.service;

import com.vikas.smart.finance.managemnet.model.Budget;
import com.vikas.smart.finance.managemnet.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.*;

/**
 * Handles CRUD operations and generates intelligent budget suggestions for users.
 */
@Service
public class BudgetService {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<Budget> budgetTable;
    private final TransactionService transactionService;

    @Autowired
    public BudgetService(DynamoDbEnhancedClient enhancedClient, TransactionService transactionService) {
        this.enhancedClient = enhancedClient;
        this.transactionService = transactionService;
        this.budgetTable = enhancedClient.table("Budget", TableSchema.fromBean(Budget.class));
    }

    /** Save or update a budget for a user */
    public void saveOrUpdateBudget(Budget budget) {
        budgetTable.putItem(budget);
    }

    /** Fetch all budgets for a given user */
    public List<Budget> getBudgets(String userId) {
        List<Budget> budgets = new ArrayList<>();
        budgetTable.query(QueryConditional.keyEqualTo(k -> k.partitionValue(userId)))
                .items()
                .forEach(budgets::add);
        return budgets;
    }

    /**
     * Generate intelligent budget suggestions based on past transactions.
     */
    public List<Budget> generateBudgetSuggestions(String userId) {
        List<Transaction> transactions = transactionService.getTransactions(userId);

        if (transactions.isEmpty()) return Collections.emptyList();

        Map<String, Double> spentPerCategory = new HashMap<>();
        for (Transaction t : transactions) {
            spentPerCategory.merge(t.getCategory(), t.getAmount(), Double::sum);
        }

        List<Budget> existingBudgets = getBudgets(userId);
        Map<String, Double> existingBudgetMap = new HashMap<>();
        for (Budget b : existingBudgets) {
            existingBudgetMap.put(b.getCategory(), b.getAmount());
        }

        List<Budget> suggestions = new ArrayList<>();

        for (Map.Entry<String, Double> entry : spentPerCategory.entrySet()) {
            String category = entry.getKey();
            double spent = entry.getValue();
            double currentBudget = existingBudgetMap.getOrDefault(category, spent);
            double suggestedBudget;

            if (spent > currentBudget * 1.1) {
                suggestedBudget = currentBudget * 1.15; // overspend → +15%
            } else if (spent < currentBudget * 0.8) {
                suggestedBudget = currentBudget * 0.9; // underspend → -10%
            } else {
                suggestedBudget = currentBudget; // within range → keep same
            }

            Budget suggestion = new Budget();
            suggestion.setUserId(userId);
            suggestion.setCategory(category);
            suggestion.setAmount(Math.round(suggestedBudget * 100.0) / 100.0); // 2 decimals
            suggestions.add(suggestion);
        }

        return suggestions; // Do NOT save automatically
    }

    /**
     * Compare user's spending to budget and return a map of % spent.
     */
    public Map<String, Double> calculateSpendingRatio(String userId) {
        List<Transaction> transactions = transactionService.getTransactions(userId);
        List<Budget> budgets = getBudgets(userId);

        Map<String, Double> spent = new HashMap<>();
        for (Transaction t : transactions) {
            spent.merge(t.getCategory(), t.getAmount(), Double::sum);
        }

        Map<String, Double> ratio = new HashMap<>();
        for (Budget b : budgets) {
            double budgetAmount = b.getAmount();
            if (budgetAmount == 0) continue;
            double used = spent.getOrDefault(b.getCategory(), 0.0);
            ratio.put(b.getCategory(), Math.round((used / budgetAmount) * 100.0 * 100.0) / 100.0); // 2 decimals %
        }

        return ratio;
    }
}
