package sopt.comfit.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sopt.comfit.auth.dto.UserInfoDTO;
import sopt.comfit.auth.service.KakaoAuthService;


@RestController
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoAuthService kakaoAuthService;

    @GetMapping("/oauth/kakao/callback")
    public UserInfoDTO kakaoCallback(
            @RequestParam("code") String code
    ) {
        UserInfoDTO userInfoDto = kakaoAuthService.getKakaoUserInfoByCode(code);
        return userInfoDto;
    }
}
