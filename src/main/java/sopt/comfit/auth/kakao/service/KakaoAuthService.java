package sopt.comfit.auth.kakao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import sopt.comfit.auth.dto.query.LoginQueryDto;
import sopt.comfit.auth.kakao.dto.KakaoTokenResponseDto;
import sopt.comfit.auth.kakao.dto.KakaoUserApiResponseDto;
import sopt.comfit.auth.dto.LoginResponseDto;
import sopt.comfit.auth.exception.AuthErrorCode;
import sopt.comfit.auth.service.AuthService;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.global.security.util.JwtUtil;
import sopt.comfit.university.domain.UniversityRepository;
import sopt.comfit.user.domain.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoAuthService {

    @Value("${kakao.client-id}") String clientId;
    @Value("${kakao.client-secret}") String clientSecret;
    @Value("${kakao.redirect-uri}") String redirectUri;
    private final AuthService authService;

    public LoginQueryDto getKakaoUserInfoByCode(String code) {
        String accessToken = getKakaoAccessToken(code);
        return getKakaoUserInfo(accessToken);
    }

    private String getKakaoAccessToken(String code) {
        log.info("=== 카카오 토큰 요청 ===");
        log.info("code: {}", code);
        log.info("redirectUri: {}", redirectUri);

        WebClient webClient = WebClient.create("https://kauth.kakao.com");

        return webClient.post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("redirect_uri", redirectUri)
                        .with("code", code))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .doOnNext(body -> log.error("카카오 에러 응답: {}", body))
                                .flatMap(body -> Mono.error(new RuntimeException("Kakao error: " + body)))
                )
                .bodyToMono(KakaoTokenResponseDto.class)
                .block()
                .access_token();
    }

    private LoginQueryDto getKakaoUserInfo(String accessToken) {
        WebClient webClient = WebClient.create("https://kapi.kakao.com");

        KakaoUserApiResponseDto response = webClient.get()
                .uri("/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserApiResponseDto.class)
                .block();

        if (response == null) {
            throw BaseException.type(AuthErrorCode.USERINFO_NOT_FOUND);
        }

        return authService.registerOrLogin(response);
    }
}
