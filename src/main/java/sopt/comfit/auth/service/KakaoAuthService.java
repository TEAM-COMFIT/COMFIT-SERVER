package sopt.comfit.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import sopt.comfit.auth.dto.KakaoTokenResponseDTO;
import sopt.comfit.auth.dto.KakaoUserApiResponseDTO;
import sopt.comfit.auth.dto.UserInfoDTO;
import sopt.comfit.auth.exception.KakaoLoginErrorCode;
import sopt.comfit.global.dto.JwtDto;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.global.security.util.JwtUtil;
import sopt.comfit.university.domain.UniversityRepository;
import sopt.comfit.user.domain.User;
import sopt.comfit.user.domain.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    @Value("${kakao.client-id}") String clientId;
    @Value("${kakao.client-secret}") String clientSecret;
    @Value("${kakao.redirect-uri}") String redirectUri;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;

    public UserInfoDTO getKakaoUserInfoByCode(String code) {
        String accessToken = getKakaoAccessToken(code);
        return getKakaoUserInfo(accessToken);
    }

    private String getKakaoAccessToken(String code) {
        WebClient webClient = WebClient.create("https://kauth.kakao.com");

        KakaoTokenResponseDTO response = webClient.post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("redirect_uri", redirectUri)
                        .with("code", code))
                .retrieve()
                .bodyToMono(KakaoTokenResponseDTO.class)
                .block();

        System.out.println("response = " + response);

        if (response == null || response.access_token() == null) {
            throw BaseException.type(KakaoLoginErrorCode.KAKAO_ACCESS_TOKEN_FAIL);
        }

        return response.access_token();
    }

    private UserInfoDTO getKakaoUserInfo(String accessToken) {

        WebClient webClient = WebClient.create("https://kapi.kakao.com");

        KakaoUserApiResponseDTO response = webClient.get()
                .uri("/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserApiResponseDTO.class)
                .block();

        if (response == null) {
            throw BaseException.type(KakaoLoginErrorCode.USERINFO_NOT_FOUND);
        }
        return registerOrLogin(response);
    }

    private UserInfoDTO registerOrLogin(KakaoUserApiResponseDTO dto) {
        Optional<User> optionalUser =
                userRepository.findByEmail(dto.kakao_account().email());

        boolean isNew = optionalUser.isEmpty();

        User user = optionalUser.orElseGet(() ->
                userRepository.save(
                        User.createKakaoUser(
                                dto.kakao_account().email(),
                                String.valueOf(dto.id()),
                                dto.kakao_account().profile().nickname()
                        )
                )
        );

        JwtDto jwtDto = jwtUtil.generateTokens(user.getId(), user.getRole());
        return UserInfoDTO.from(user, isNew, jwtDto);
    }
}
