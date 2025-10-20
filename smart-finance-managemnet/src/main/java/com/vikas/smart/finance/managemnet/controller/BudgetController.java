package com.vikas.smart.finance.managemnet.controller;

import com.vikas.smart.finance.managemnet.model.Budget;
import com.vikas.smart.finance.managemnet.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    // GET all budgets for a user
    @GetMapping("/{userId}")
    public List<Budget> getBudgets(@PathVariable String userId) {
        return budgetService.getBudgets(userId);
    }

    // POST endpoint to add or update a budget
    @PostMapping
    public String addOrUpdateBudget(@RequestBody Budget budget) {
        budgetService.saveOrUpdateBudget(budget);
        return "Budget saved successfully for user: " + budget.getUserId();
    }

    // GET budget suggestions for a user
    @GetMapping("/suggestions/{userId}")
    public List<Budget> getBudgetSuggestions(@PathVariable String userId) {
        return budgetService.generateBudgetSuggestions(userId);
    }
}
