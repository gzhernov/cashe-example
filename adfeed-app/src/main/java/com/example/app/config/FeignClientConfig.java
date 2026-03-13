package com.example.app.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class FeignClientConfig {

    @Bean("feignObjectMapper")
    @Primary
    public ObjectMapper feignObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // НЕ включаем defaultTyping - для обычных HTTP запросов он не нужен
        return mapper;
    }

    @Bean
    public Decoder feignDecoder(@Qualifier("feignObjectMapper") ObjectMapper feignObjectMapper) {
        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter(feignObjectMapper);
        ObjectFactory<HttpMessageConverters> objectFactory =
                () -> new HttpMessageConverters(converter);
        return new ResponseEntityDecoder(new SpringDecoder(objectFactory));
    }

    @Bean
    public Encoder feignEncoder(@Qualifier("feignObjectMapper") ObjectMapper feignObjectMapper) {
        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter(feignObjectMapper);
        ObjectFactory<HttpMessageConverters> objectFactory =
                () -> new HttpMessageConverters(converter);
        return new SpringEncoder(objectFactory);
    }
}