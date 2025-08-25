package com.laijiaxiang.supreme.config;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * jackson全局配置java8 LocalDateTime的序列化 全局配置时间返回格式
 */
@Configuration
public class JacksonCustomizerConfig {

    @Bean
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper supremeJsonMapper(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
        ObjectMapper objectMapper = jackson2ObjectMapperBuilder.createXmlMapper(false).build();
        objectMapper.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {
            @Override
            public void serialize(Object object, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                JsonStreamContext outputContext = jsonGenerator.getOutputContext();
                Object currentValue = outputContext.getCurrentValue();// 这里可以获取到被序列化的对象
                String currentName = outputContext.getCurrentName(); // 这里获取了序列化的属性
                // 一个被序列化的对象找到了，这个当前序列化的属性也找到了，所以如果借用反射方式可以获取当前的类型
                if (currentValue != null) {
                    Field findField = ReflectionUtils.findField(currentValue.getClass(), currentName);
                    Class<?> filedType = findField.getType();// 获取字段的类型
                    //判断类型
                    if (filedType.isArray() || filedType.isAssignableFrom(List.class)) {
                        // 数组 或 List集合
                        jsonGenerator.writeObject(new ArrayList<>());
                    } else if (filedType.isAssignableFrom(String.class)) {
                        // 字符串
                        jsonGenerator.writeObject("");
                    } else if (filedType.isAssignableFrom(Boolean.class)) {
                        // 布尔类型
                        jsonGenerator.writeObject(false);
                    } else if (filedType.isAssignableFrom(Integer.class)) {
                        // 整形
                        jsonGenerator.writeObject(0);
                    } else if (filedType.isAssignableFrom(Long.class)) {
                        // 长整形
                        jsonGenerator.writeObject("");
                    } else if (filedType.isAssignableFrom(Double.class)) {
                        // 双精度浮点型
                        jsonGenerator.writeObject(0.00d);
                    } else if (filedType.isAssignableFrom(Float.class)) {
                        // 单精度浮点型
                        jsonGenerator.writeObject(0.00f);
                    } else if (filedType.isAssignableFrom(BigDecimal.class)) {
                        // BigDecimal类
                        jsonGenerator.writeObject(0.00d);
                    } else if (filedType.isAssignableFrom(LocalDateTime.class)
                            || filedType.isAssignableFrom(LocalDate.class)) {
                        // 时间类型
                        jsonGenerator.writeObject(0);
                    } else {
                        // 其他对象
                        jsonGenerator.writeObject(new HashMap<>());
                    }
                }
            }
        });
        return objectMapper;
    }

    /**
     * description:适配自定义序列化和反序列化策略，返回前端指定数据类型的数据
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            builder.serializerByType(Long.class, new LongSerializer());
            builder.deserializerByType(Long.class, new LongDeserializer());
        };
    }

    /**
     * description:序列化
     * Long转String
     */
    public static class LongSerializer extends JsonSerializer<Long> {
        @Override
        public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            if (!ObjectUtils.isEmpty(value)) {
                String result = String.valueOf(value);
                gen.writeString(result);
            }
        }
    }

    /**
     * description:反序列化
     * String转Long
     */
    public static class LongDeserializer extends JsonDeserializer<Long> {
        @Override
        public Long deserialize(JsonParser p, DeserializationContext deserializationContext)
                throws IOException {
            String strValue = p.getValueAsString();
            if (!ObjectUtils.isEmpty(strValue)) {
                Long result = Long.valueOf(strValue);
                return result;
            } else {
                return null;
            }
        }
    }
}

