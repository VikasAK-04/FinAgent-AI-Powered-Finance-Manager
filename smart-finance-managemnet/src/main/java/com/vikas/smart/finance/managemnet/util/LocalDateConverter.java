package com.vikas.smart.finance.managemnet.util;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateConverter implements AttributeConverter<LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public AttributeValue transformFrom(LocalDate input) {
        return AttributeValue.builder().s(input.format(FORMATTER)).build();
    }

    @Override
    public LocalDate transformTo(AttributeValue input) {
        return LocalDate.parse(input.s(), FORMATTER);
    }

    @Override
    public EnhancedType<LocalDate> type() {
        return EnhancedType.of(LocalDate.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
