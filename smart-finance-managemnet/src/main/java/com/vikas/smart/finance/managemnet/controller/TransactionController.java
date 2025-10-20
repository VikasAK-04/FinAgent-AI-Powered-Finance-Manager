package com.vikas.smart.finance.managemnet.controller;

import com.vikas.smart.finance.managemnet.model.Transaction;
import com.vikas.smart.finance.managemnet.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // GET all transactions for a user
    @GetMapping("/{userId}")
    public List<Transaction> getTransactions(@PathVariable String userId){
        return transactionService.getTransactions(userId);
    }

    // POST a new transaction (optional for testing)
    @PostMapping
    public String addTransaction(@RequestBody Transaction transaction){
        transactionService.saveTransaction(transaction);
        return "Transaction added successfully for user: " + transaction.getUserId();
    }
}
