package com.vikas.smart.finance.managemnet.service.agentic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vikas.smart.finance.managemnet.model.Transaction;
import com.vikas.smart.finance.managemnet.model.Budget;
import com.vikas.smart.finance.managemnet.service.TransactionService;
import com.vikas.smart.finance.managemnet.service.BudgetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.*;

/**
 * Simplified Bedrock Service - Uses FREE Direct Claude API
 * No Agent needed - Works immediately!
 */
@Service
public class BedrockAgentService {

    private static final Logger log = LoggerFactory.getLogger(BedrockAgentService.class);

    private final BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper;

    private final com.vikas.smart.finance.managemnet.service.TransactionService transactionService;
    private final com.vikas.smart.finance.managemnet.service.BudgetService budgetService;

    @Autowired
    public BedrockAgentService(BedrockRuntimeClient bedrockClient,
                               com.vikas.smart.finance.managemnet.service.TransactionService transactionService,
                               com.vikas.smart.finance.managemnet.service.BudgetService budgetService) {
        this.bedrockClient = bedrockClient;
        this.transactionService = transactionService;
        this.budgetService = budgetService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get AI response using FREE Claude 3 Haiku
     * Uses direct model invocation (no agent needed)
     */
    public String getAIResponse(String prompt) {
        try {
            log.info("Invoking Claude 3 Haiku with prompt: {}", prompt);

            // Build request for Claude
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("anthropic_version", "bedrock-2023-05-31");
            requestBody.put("max_tokens", 1000);

            // Add message
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.put("messages", messages);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            log.debug("Request body: {}", jsonBody);

            // Invoke Claude 3 Haiku (FREE model)
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId("anthropic.claude-3-haiku-20240307-v1:0")
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(jsonBody))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();

            log.debug("Response body: {}", responseBody);

            // Parse response
            Map<String, Object> jsonResponse = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> content = (List<Map<String, Object>>) jsonResponse.get("content");

            if (content != null && !content.isEmpty()) {
                String aiResponse = (String) content.get(0).get("text");
                log.info("AI response received successfully");
                return aiResponse;
            }

            log.warn("No content in AI response");
            return "I apologize, but I couldn't generate a response. Please try again.";

        } catch (Exception e) {
            log.error("Error invoking Claude model", e);
            return "Error: Unable to get AI response. " + e.getMessage();
        }
    }

    /**
     * Get personalized financial advice WITH REAL DATA
     */
    public String getFinancialAdvice(String userId, String query) {
        // Fetch real data from DynamoDB
        List<com.vikas.smart.finance.managemnet.model.Transaction> transactions =
                transactionService.getTransactions(userId);
        List<com.vikas.smart.finance.managemnet.model.Budget> budgets =
                budgetService.getBudgets(userId);

        // Calculate spending by category
        Map<String, Double> spendingByCategory = new HashMap<>();
        double totalSpending = 0.0;
        for (com.vikas.smart.finance.managemnet.model.Transaction t : transactions) {
            spendingByCategory.merge(t.getCategory(), t.getAmount(), Double::sum);
            totalSpending += t.getAmount();
        }

        // Build context-rich prompt
        StringBuilder context = new StringBuilder();
        context.append("USER FINANCIAL DATA FOR ").append(userId).append(":\n\n");
        context.append("TOTAL SPENDING: $").append(String.format("%.2f", totalSpending)).append("\n\n");
        context.append("SPENDING BY CATEGORY:\n");
        for (Map.Entry<String, Double> entry : spendingByCategory.entrySet()) {
            context.append("- ").append(entry.getKey()).append(": $")
                    .append(String.format("%.2f", entry.getValue())).append("\n");
        }

        if (!budgets.isEmpty()) {
            context.append("\nBUDGETS:\n");
            for (com.vikas.smart.finance.managemnet.model.Budget b : budgets) {
                double spent = spendingByCategory.getOrDefault(b.getCategory(), 0.0);
                double percentage = b.getAmount() > 0 ? (spent / b.getAmount() * 100) : 0;
                context.append("- ").append(b.getCategory()).append(": $")
                        .append(String.format("%.2f", spent)).append(" / $")
                        .append(String.format("%.2f", b.getAmount()))
                        .append(" (").append(String.format("%.0f", percentage)).append("%)\n");
            }
        }

        context.append("\nTOTAL TRANSACTIONS: ").append(transactions.size()).append("\n\n");
        context.append("USER QUESTION: ").append(query).append("\n\n");
        context.append("Provide specific, actionable financial advice based on this real data. ");
        context.append("Be encouraging but honest. Use bullet points for clarity.");

        String enhancedPrompt = context.toString();

        return getAIResponse(enhancedPrompt);
    }

    /**
     * Analyze spending with context
     */
    public String analyzeSpending(String userId, Map<String, Object> spendingData) {
        String prompt = String.format(
                "You are a financial analyst. Analyze this spending data for user %s:\n\n%s\n\n" +
                        "Provide:\n" +
                        "1. Key observations\n" +
                        "2. Areas of concern\n" +
                        "3. 3 specific recommendations\n" +
                        "Be concise and actionable.",
                userId, formatDataForPrompt(spendingData)
        );

        return getAIResponse(prompt);
    }

    /**
     * Generate budget recommendations
     */
    public String recommendBudget(String userId, Map<String, Object> currentData) {
        String prompt = String.format(
                "You are a budget planning expert. Based on this financial data for user %s:\n\n%s\n\n" +
                        "Suggest optimal budget allocations for each spending category. " +
                        "Explain your reasoning briefly.",
                userId, formatDataForPrompt(currentData)
        );

        return getAIResponse(prompt);
    }

    /**
     * Generate comprehensive health report
     */
    public String generateHealthReport(String userId,
                                       Map<String, Object> spending,
                                       Map<String, Object> budget,
                                       Map<String, Object> insights) {
        String prompt = String.format(
                "You are a financial health advisor. Generate a comprehensive report for user %s.\n\n" +
                        "SPENDING DATA:\n%s\n\n" +
                        "BUDGET STATUS:\n%s\n\n" +
                        "INSIGHTS:\n%s\n\n" +
                        "Provide:\n" +
                        "1. Overall financial health score (1-10)\n" +
                        "2. Top 3 strengths\n" +
                        "3. Top 3 concerns\n" +
                        "4. 5 actionable recommendations\n\n" +
                        "Be encouraging but honest.",
                userId,
                formatDataForPrompt(spending),
                formatDataForPrompt(budget),
                formatDataForPrompt(insights)
        );

        return getAIResponse(prompt);
    }

    /**
     * Helper: Format data for prompt (simple string representation)
     */
    private String formatDataForPrompt(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}