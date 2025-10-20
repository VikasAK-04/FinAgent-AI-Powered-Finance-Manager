package com.vikas.smart.finance.managemnet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Configuration
public class AwsConfig {

    private final AwsProperties awsProperties;

    public AwsConfig(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
    }

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.EU_NORTH_1)  // Your DynamoDB region
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        awsProperties.getAccessKeyId(),
                                        awsProperties.getSecretKey()
                                )
                        )
                )
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient() {
        // Direct Claude access (FREE!)
        return BedrockRuntimeClient.builder()
                .region(Region.US_EAST_1)  // Bedrock available here
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        awsProperties.getAccessKeyId(),
                                        awsProperties.getSecretKey()
                                )
                        )
                )
                .build();
    }
}