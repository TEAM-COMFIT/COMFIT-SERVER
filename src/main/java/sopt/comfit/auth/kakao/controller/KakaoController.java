package sopt.comfit.auth.kakao.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sopt.comfit.auth.dto.UserInfoDto;
import sopt.comfit.auth.kakao.service.KakaoAuthService;


@RestController
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoAuthService kakaoAuthService;

    @GetMapping("/oauth/kakao/callback")
    public UserInfoDto kakaoCallback(
            @RequestParam("code") String code
    ) {
        UserInfoDto userInfoDto = kakaoAuthService.getKakaoUserInfoByCode(code);
        return userInfoDto;
    }
}
