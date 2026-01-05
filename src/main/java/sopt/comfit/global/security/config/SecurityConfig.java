package sopt.comfit.global.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sopt.comfit.global.constants.Constants;
import sopt.comfit.global.security.exception.CustomAccessDeniedHandler;
import sopt.comfit.global.security.exception.CustomAuthenticationEntryPointerHandler;
import sopt.comfit.global.security.filter.JwtAuthenticationFilter;
import sopt.comfit.global.security.filter.JwtExceptionFilter;
import sopt.comfit.global.security.manager.JwtAuthenticationManager;
import sopt.comfit.global.security.util.JwtUtil;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationEntryPointerHandler customAuthenticationEntryPointerHandler;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtExceptionFilter jwtExceptionFilter;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(new PortRequestMatcher(8080))

                .csrf(AbstractHttpConfigurer::disable)

                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .formLogin(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(request ->
                        request.requestMatchers(Constants.NO_NEED_AUTH.toArray(String[]::new)).permitAll()
                                .anyRequest().authenticated())

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPointerHandler)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class
                )

                .addFilterBefore(
                        jwtExceptionFilter, JwtAuthenticationFilter.class
                )

                .getOrBuild();

    }
}
