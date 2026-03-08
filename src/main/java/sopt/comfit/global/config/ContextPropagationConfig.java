package sopt.comfit.global.config;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshotFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Hooks;

@Slf4j
@Configuration
public class ContextPropagationConfig {

    @PostConstruct
    public void init() {
        Hooks.enableAutomaticContextPropagation();

        ContextRegistry.getInstance().registerThreadLocalAccessor(
                "security.context",
                SecurityContextHolder::getContext,
                SecurityContextHolder::setContext,
                SecurityContextHolder::clearContext
        );

        // MDC 등록
        ContextRegistry.getInstance().registerThreadLocalAccessor(
                "mdc",
                MDC::getCopyOfContextMap,
                context -> {
                    if (context != null) {
                        MDC.setContextMap(context);
                    }
                },
                MDC::clear
        );

        log.info("Context Propagation 활성화 - MDC, SecurityContext 등록 완료");


    }

    @Bean
    public ContextSnapshotFactory contextSnapshotFactory() {
        return ContextSnapshotFactory.builder().build();
    }
}