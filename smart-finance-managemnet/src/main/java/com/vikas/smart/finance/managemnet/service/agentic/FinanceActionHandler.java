package com.vikas.smart.finance.managemnet.service.agentic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vikas.smart.finance.managemnet.model.Budget;
import com.vikas.smart.finance.managemnet.model.Insight;
import com.vikas.smart.finance.managemnet.model.Transaction;
import com.vikas.smart.finance.managemnet.service.BudgetService;
import com.vikas.smart.finance.managemnet.service.InsightService;
import com.vikas.smart.finance.managemnet.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles action group functions that the Bedrock Agent can invoke
 */
@Service
public class FinanceActionHandler {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private InsightService insightService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get spending summary for a user
     */
    public Map<String, Object> getSpendingSummary(String userId) {
        List<Transaction> transactions = transactionService.getTransactions(userId);

        Map<String, Double> categorySpending = transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        double totalSpending = categorySpending.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("totalSpending", Math.round(totalSpending * 100.0) / 100.0);
        result.put("categoryBreakdown", categorySpending);
        result.put("transactionCount", transactions.size());

        return result;
    }

    /**
     * Get budget status for a user
     */
    public Map<String, Object> getBudgetStatus(String userId) {
        List<Budget> budgets = budgetService.getBudgets(userId);
        Map<String, Double> spendingRatio = budgetService.calculateSpendingRatio(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("budgets", budgets);
        result.put("spendingRatios", spendingRatio);

        // Calculate alerts
        List<String> alerts = new ArrayList<>();
        for (Map.Entry<String, Double> entry : spendingRatio.entrySet()) {
            if (entry.getValue() >= 100) {
                alerts.add("⚠️ EXCEEDED: " + entry.getKey() + " (" + entry.getValue() + "%)");
            } else if (entry.getValue() >= 80) {
                alerts.add("⚡ WARNING: " + entry.getKey() + " (" + entry.getValue() + "%)");
            }
        }
        result.put("alerts", alerts);

        return result;
    }

    /**
     * Get AI-powered insights for a user
     */
    public Map<String, Object> getFinancialInsights(String userId) {
        List<Insight> insights = insightService.getInsights(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("totalInsights", insights.size());
        result.put("insights", insights);

        // Group by type
        Map<String, Long> insightsByType = insights.stream()
                .collect(Collectors.groupingBy(Insight::getType, Collectors.counting()));
        result.put("insightsByType", insightsByType);

        return result;
    }

    /**
     * Get smart budget recommendations
     */
    public Map<String, Object> getBudgetRecommendations(String userId) {
        List<Budget> suggestions = budgetService.generateBudgetSuggestions(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("recommendations", suggestions);
        result.put("totalCategories", suggestions.size());

        return result;
    }

    /**
     * Get top spending vendors
     */
    public Map<String, Object> getTopVendors(String userId, int limit) {
        List<Transaction> transactions = transactionService.getTransactions(userId);

        Map<String, Double> vendorSpending = transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getVendor,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        List<Map.Entry<String, Double>> topVendors = vendorSpending.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("topVendors", topVendors);

        return result;
    }

    /**
     * Analyze spending trends
     */
    public Map<String, Object> analyzeSpendingTrends(String userId) {
        List<Transaction> transactions = transactionService.getTransactions(userId);

        if (transactions.isEmpty()) {
            return Map.of("userId", userId, "message", "No transactions found");
        }

        double avgTransaction = transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .average()
                .orElse(0.0);

        double maxTransaction = transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .max()
                .orElse(0.0);

        double minTransaction = transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .min()
                .orElse(0.0);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("averageTransaction", Math.round(avgTransaction * 100.0) / 100.0);
        result.put("maxTransaction", maxTransaction);
        result.put("minTransaction", minTransaction);
        result.put("totalTransactions", transactions.size());

        return result;
    }
}