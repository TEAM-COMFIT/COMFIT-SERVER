package sopt.comfit.report.infra.feign;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class OpenAiFeignClientConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor authInterceptor() {
        return template ->
        {template.header("Authorization", "Bearer " + apiKey);};
    }
}