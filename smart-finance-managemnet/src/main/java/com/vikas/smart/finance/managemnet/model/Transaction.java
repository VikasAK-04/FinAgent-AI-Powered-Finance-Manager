package com.vikas.smart.finance.managemnet.model;

import com.vikas.smart.finance.managemnet.util.LocalDateConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import java.time.LocalDate;

@DynamoDbBean
public class Transaction {

    private String userId;          // Partition Key
    private String transactionId;   // Sort Key
    private String vendor;
    private String category;
    private double amount;
    private LocalDate date;

    public Transaction() {}

    @DynamoDbPartitionKey
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @DynamoDbSortKey
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    @DynamoDbAttribute("vendor")
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    @DynamoDbAttribute("category")
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @DynamoDbAttribute("amount")
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    @DynamoDbConvertedBy(LocalDateConverter.class)
    @DynamoDbAttribute("date")
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
