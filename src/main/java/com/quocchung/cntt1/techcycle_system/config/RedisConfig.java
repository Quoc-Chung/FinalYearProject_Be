package com.quocchung.cntt1.techcycle_system.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfig {

  @Bean
  public ObjectMapper redisObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    mapper.activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance,
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.PROPERTY
    );

    return mapper;
  }

  @Bean
  @Primary
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory connectionFactory,
      ObjectMapper redisObjectMapper) {

    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    StringRedisSerializer keySerializer = new StringRedisSerializer();

    Jackson2JsonRedisSerializer<Object> valueSerializer =
        new Jackson2JsonRedisSerializer<>(redisObjectMapper, Object.class);

    template.setKeySerializer(keySerializer);
    template.setHashKeySerializer(keySerializer);
    template.setValueSerializer(valueSerializer);
    template.setHashValueSerializer(valueSerializer);

    template.afterPropertiesSet();

    return template;
  }
}