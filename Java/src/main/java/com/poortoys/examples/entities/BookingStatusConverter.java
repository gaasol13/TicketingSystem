package com.poortoys.examples.entities;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

// Custom converter to handle the string conversion
@Converter(autoApply = true)
public class BookingStatusConverter implements AttributeConverter<BookingStatus, String> {
    @Override
    public String convertToDatabaseColumn(BookingStatus status) {
        return status != null ? status.getValue() : null;
    }

    @Override
    public BookingStatus convertToEntityAttribute(String status) {
        return BookingStatus.fromString(status);
    }
}