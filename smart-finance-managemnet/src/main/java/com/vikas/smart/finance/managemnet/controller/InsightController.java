package com.vikas.smart.finance.managemnet.controller;

import com.vikas.smart.finance.managemnet.model.Insight;
import com.vikas.smart.finance.managemnet.service.InsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
public class InsightController {

    @Autowired
    private InsightService insightService;

    // GET all insights for a user
    @GetMapping("/{userId}")
    public List<Insight> getInsights(@PathVariable String userId) {
        // Assuming InsightService has a method to fetch insights from DynamoDB
        return insightService.getInsights(userId);
    }

    // POST endpoint to manually trigger insight generation for a user (optional)
    @PostMapping("/generate/{userId}")
    public String generateInsights(@PathVariable String userId) {
        insightService.generateInsights(userId);
        return "Insights generated successfully for user: " + userId;
    }
}
