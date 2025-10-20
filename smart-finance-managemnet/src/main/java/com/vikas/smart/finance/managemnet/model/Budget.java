package com.vikas.smart.finance.managemnet.model;

import com.vikas.smart.finance.managemnet.util.LocalDateConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import java.time.LocalDate;

@DynamoDbBean
public class Budget {

    private String userId;     // Partition Key
    private String category;   // Sort Key
    private double amount;
    private LocalDate lastUpdated;

    public Budget() {}

    @DynamoDbPartitionKey
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @DynamoDbSortKey
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @DynamoDbAttribute("amount")
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    @DynamoDbConvertedBy(LocalDateConverter.class)
    @DynamoDbAttribute("lastUpdated")
    public LocalDate getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDate lastUpdated) { this.lastUpdated = lastUpdated; }
}
