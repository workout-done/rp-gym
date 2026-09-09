package com.workoutdone.rpgym.user.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

// 요청 바디의 문자열 값 앞뒤 공백을 역직렬화 시점에 제거해, Bean Validation이 트리밍된 값을 검증하도록 함
public class TrimmingStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        return value == null ? null : value.trim();
    }
}
