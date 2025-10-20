package com.vikas.smart.finance.managemnet.controller;

import com.vikas.smart.finance.managemnet.service.agentic.BedrockAgentService;
import com.vikas.smart.finance.managemnet.service.agentic.FinanceActionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class EnhancedAgenticController {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedAgenticController.class);

    @Autowired
    private BedrockAgentService bedrockAgentService;

    @Autowired
    private FinanceActionHandler financeActionHandler;

    /**
     * Main AI chat endpoint
     * Example: GET /ai/chat?userId=user1&prompt=What's my spending like?
     */
    @GetMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(
            @RequestParam String userId,
            @RequestParam String prompt) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (prompt == null || prompt.trim().isEmpty()) {
                response.put("error", "Prompt cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }

            // Use getFinancialAdvice instead - it includes real data!
            String aiResponse = bedrockAgentService.getFinancialAdvice(userId, prompt);

            response.put("userId", userId);
            response.put("prompt", prompt);
            response.put("response", aiResponse);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in AI chat for user '{}'", userId, e);
            response.put("error", "Failed to get AI response: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get AI-powered spending analysis
     */
    @GetMapping("/analyze/spending/{userId}")
    public ResponseEntity<Map<String, Object>> analyzeSpending(@PathVariable String userId) {
        try {
            Map<String, Object> spendingData = financeActionHandler.getSpendingSummary(userId);

            String prompt = String.format(
                    "Analyze this spending data and provide insights: %s",
                    spendingData.toString()
            );

            String aiAnalysis = bedrockAgentService.getAIResponse(prompt);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("spendingData", spendingData);
            response.put("aiAnalysis", aiAnalysis);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error analyzing spending for user '{}'", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get AI-powered budget recommendations
     */
    @GetMapping("/recommend/budget/{userId}")
    public ResponseEntity<Map<String, Object>> recommendBudget(@PathVariable String userId) {
        try {
            Map<String, Object> budgetData = financeActionHandler.getBudgetRecommendations(userId);
            Map<String, Object> spendingData = financeActionHandler.getSpendingSummary(userId);

            String prompt = String.format(
                    "Based on spending: %s and current budgets: %s, provide smart budget recommendations.",
                    spendingData, budgetData
            );

            String aiRecommendation = bedrockAgentService.getAIResponse(prompt);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("budgetData", budgetData);
            response.put("aiRecommendation", aiRecommendation);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error generating budget recommendations for user '{}'", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get AI financial advisor response
     */
    @PostMapping("/advisor")
    public ResponseEntity<Map<String, Object>> getFinancialAdvice(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String question = request.get("question");

            if (userId == null || question == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "userId and question are required"));
            }

            String advice = bedrockAgentService.getFinancialAdvice(userId, question);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("question", question);
            response.put("advice", advice);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting financial advice", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get comprehensive financial health report
     */
    @GetMapping("/health-report/{userId}")
    public ResponseEntity<Map<String, Object>> getHealthReport(@PathVariable String userId) {
        try {
            Map<String, Object> spending = financeActionHandler.getSpendingSummary(userId);
            Map<String, Object> budget = financeActionHandler.getBudgetStatus(userId);
            Map<String, Object> insights = financeActionHandler.getFinancialInsights(userId);
            Map<String, Object> trends = financeActionHandler.analyzeSpendingTrends(userId);

            String prompt = String.format(
                    "Generate a comprehensive financial health report for user %s. " +
                            "Spending: %s, Budget Status: %s, Insights: %s, Trends: %s. " +
                            "Provide actionable advice and highlight concerns.",
                    userId, spending, budget, insights, trends
            );

            String healthReport = bedrockAgentService.getAIResponse(prompt);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("spending", spending);
            response.put("budget", budget);
            response.put("insights", insights);
            response.put("trends", trends);
            response.put("aiHealthReport", healthReport);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error generating health report for user '{}'", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Quick action endpoint for common queries
     */
    @GetMapping("/quick/{action}/{userId}")
    public ResponseEntity<Map<String, Object>> quickAction(
            @PathVariable String action,
            @PathVariable String userId) {

        try {
            Map<String, Object> result;

            switch (action.toLowerCase()) {
                case "summary":
                    result = financeActionHandler.getSpendingSummary(userId);
                    break;
                case "budget":
                    result = financeActionHandler.getBudgetStatus(userId);
                    break;
                case "insights":
                    result = financeActionHandler.getFinancialInsights(userId);
                    break;
                case "trends":
                    result = financeActionHandler.analyzeSpendingTrends(userId);
                    break;
                case "vendors":
                    result = financeActionHandler.getTopVendors(userId, 5);
                    break;
                default:
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Unknown action: " + action));
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error executing quick action '{}' for user '{}'", action, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}