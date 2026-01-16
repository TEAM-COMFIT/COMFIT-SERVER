package sopt.comfit.auth.kakao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import sopt.comfit.auth.kakao.dto.KakaoTokenResponseDto;
import sopt.comfit.auth.kakao.dto.KakaoUserApiResponseDto;
import sopt.comfit.auth.dto.UserInfoDto;
import sopt.comfit.auth.kakao.exception.KakaoLoginErrorCode;
import sopt.comfit.auth.service.AuthService;
import sopt.comfit.global.dto.JwtDto;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.global.security.util.JwtUtil;
import sopt.comfit.university.domain.UniversityRepository;
import sopt.comfit.user.domain.User;
import sopt.comfit.user.domain.UserRepository;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    @Value("${kakao.client-id}") String clientId;
    @Value("${kakao.client-secret}") String clientSecret;
    @Value("${kakao.redirect-uri}") String redirectUri;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final AuthService authService;

    public UserInfoDto getKakaoUserInfoByCode(String code) {
        String accessToken = getKakaoAccessToken(code);
        return getKakaoUserInfo(accessToken);
    }

    private String getKakaoAccessToken(String code) {
        WebClient webClient = WebClient.create("https://kauth.kakao.com");

        KakaoTokenResponseDto response = webClient.post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("redirect_uri", redirectUri)
                        .with("code", code))
                .retrieve()
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();

        System.out.println("response = " + response);

        if (response == null || response.access_token() == null) {
            throw BaseException.type(KakaoLoginErrorCode.KAKAO_ACCESS_TOKEN_FAIL);
        }

        return response.access_token();
    }

    private UserInfoDto getKakaoUserInfo(String accessToken) {

        WebClient webClient = WebClient.create("https://kapi.kakao.com");

        KakaoUserApiResponseDto response = webClient.get()
                .uri("/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserApiResponseDto.class)
                .block();

        if (response == null) {
            throw BaseException.type(KakaoLoginErrorCode.USERINFO_NOT_FOUND);
        }
        return authService.registerOrLogin(response);
    }
}
