package com.example.quantserver.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${ai-server.base-url}")
    private String aiServerBaseUrl;

    @Bean
    public RestClient aiServerRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30_000);
        factory.setReadTimeout(30_000);

        return RestClient.builder()
                .baseUrl(aiServerBaseUrl)
                .requestFactory(factory)
                .build();
    }
}