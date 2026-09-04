package com.workoutdone.rpgym.health.outbox.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * event_outbox.event_type을 명세 그대로 PascalCase("HealthActivitySynced")로 저장한다.
 *
 * EventOutbox가 @Convert로 직접 참조하므로 엔티티와 같은 domain 패키지에 둔다.
 * (domain → adapter 역방향 의존을 만들지 않기 위함)
 */
@Converter
public class HealthEventTypeConverter
        implements AttributeConverter<HealthEventType, String> {

    @Override
    public String convertToDatabaseColumn(HealthEventType eventType) {
        return eventType == null ? null : eventType.getEventName();
    }

    @Override
    public HealthEventType convertToEntityAttribute(String eventName) {
        return eventName == null ? null : HealthEventType.from(eventName);
    }
}