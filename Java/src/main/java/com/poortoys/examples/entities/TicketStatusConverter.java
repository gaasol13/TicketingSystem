package com.poortoys.examples.entities;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class TicketStatusConverter implements AttributeConverter<TicketStatus, String> {
    @Override
    public String convertToDatabaseColumn(TicketStatus status) {
        return status == null ? null : status.getDbValue();
    }

    @Override
    public TicketStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TicketStatus.fromDbValue(dbData);
    }
}
