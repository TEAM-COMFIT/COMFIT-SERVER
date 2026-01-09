package sopt.comfit.report.infra;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class OpenAiClientConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor authInterceptor() {
        return template ->
        {template.header("Authorization", "Bearer " + apiKey);};
    }
}