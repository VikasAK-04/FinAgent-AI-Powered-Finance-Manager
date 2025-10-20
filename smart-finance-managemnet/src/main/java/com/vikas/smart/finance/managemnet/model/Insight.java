package com.vikas.smart.finance.managemnet.model;

import com.vikas.smart.finance.managemnet.util.LocalDateConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import java.time.LocalDate;

@DynamoDbBean
public class Insight {

    private String userId;  // Partition Key
    private String type;    // Sort Key
    private String message;
    private LocalDate date;

    public Insight() {}

    @DynamoDbPartitionKey
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @DynamoDbSortKey
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @DynamoDbAttribute("message")
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @DynamoDbConvertedBy(LocalDateConverter.class)
    @DynamoDbAttribute("date")
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
