package com.vikas.smart.finance.managemnet.service;

import com.vikas.smart.finance.managemnet.model.Budget;
import com.vikas.smart.finance.managemnet.model.Insight;
import com.vikas.smart.finance.managemnet.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InsightService {

    private final TransactionService transactionService;

    @Autowired
    @Lazy
    private final BudgetService budgetService;
    private final DynamoDbTable<Insight> insightTable;

    @Autowired
    public InsightService(TransactionService transactionService,
                          BudgetService budgetService,
                          DynamoDbEnhancedClient enhancedClient) {
        this.transactionService = transactionService;
        this.budgetService = budgetService;
        this.insightTable = enhancedClient.table("Insight", TableSchema.fromBean(Insight.class));
    }

    public List<Insight> getInsights(String userId) {
        List<Insight> insights = new ArrayList<>();
        QueryConditional query = QueryConditional.keyEqualTo(k -> k.partitionValue(userId));
        insightTable.query(query).items().forEach(insights::add);
        return insights;
    }

    public void generateInsights(String userId) {
        List<Transaction> transactions = transactionService.getTransactions(userId);
        if (transactions.isEmpty()) return;

        List<Budget> budgets = budgetService.getBudgets(userId);

        Map<String, Double> spendingByCategory = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)));

        List<Insight> insights = new ArrayList<>();

        for (Budget budget : budgets) {
            double spent = spendingByCategory.getOrDefault(budget.getCategory(), 0.0);
            double limit = budget.getAmount();
            double ratio = limit > 0 ? (spent / limit) * 100.0 : 0;

            if (ratio >= 100) {
                Insight critical = new Insight();
                critical.setUserId(userId);
                critical.setType("Budget Exceeded");
                critical.setMessage("🚨 Exceeded budget for " + budget.getCategory() +
                        " (Spent: " + spent + ", Limit: " + limit + ")");
                critical.setDate(LocalDate.now());
                insights.add(critical);
            } else if (ratio >= 75) {
                Insight warning = new Insight();
                warning.setUserId(userId);
                warning.setType("Budget Warning");
                warning.setMessage("⚠️ Used " + Math.round(ratio) + "% of " + budget.getCategory() +
                        " budget (" + spent + "/" + limit + ")");
                warning.setDate(LocalDate.now());
                insights.add(warning);
            }
        }

        // Recurring vendor detection
        Map<String, Long> recurring = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getVendor, Collectors.counting()));

        recurring.forEach((vendor, count) -> {
            if (count >= 2) {
                Insight recurringPayment = new Insight();
                recurringPayment.setUserId(userId);
                recurringPayment.setType("Recurring Payment");
                recurringPayment.setMessage("💡 Detected recurring payments to " + vendor);
                recurringPayment.setDate(LocalDate.now());
                insights.add(recurringPayment);
            }
        });

        insights.forEach(insightTable::putItem);
    }

    public List<Insight> getInsightsByType(String userId, String type) {
        List<Insight> all = getInsights(userId);
        List<Insight> filtered = new ArrayList<>();
        for (Insight i : all) {
            if (i.getType().equalsIgnoreCase(type)) filtered.add(i);
        }
        return filtered;
    }
}
