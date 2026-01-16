package sopt.comfit.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sopt.comfit.auth.dto.LoginUserInfoDto;
import sopt.comfit.auth.dto.command.LoginCommandDto;
import sopt.comfit.auth.dto.request.LoginRequestDto;
import sopt.comfit.auth.dto.request.OnBoardingRequestDTO;
import sopt.comfit.auth.kakao.service.KakaoAuthService;
import sopt.comfit.auth.dto.request.ReIssueTokenRequestDto;
import sopt.comfit.auth.service.AuthService;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.global.dto.JwtDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;
    private final KakaoAuthService kakaoAuthService;

    @PostMapping("/login")
    public JwtDto join(
            @RequestBody @Valid LoginRequestDto request
    ){
        return authService.login(LoginCommandDto.from(request));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "JWT")
    public void logout(
            @LoginUser Long userId
    ){
        authService.logout(userId);
    }

    @PostMapping("/re-issued")
    @Operation(summary = "액세스 토큰 재발급", description = "RefreshToken을 사용하여 새로운 AccessToken과 RefreshToken을 발급합니다.")
    @Tag(name = "인증")
    public JwtDto reissueToken(
            @RequestBody @Valid ReIssueTokenRequestDto request
    ) {
        return authService.reissueToken(request.refreshToken());
    }

    @PostMapping("/on-boarding")
    public void addUserInfo(
            @LoginUser Long userId,
            @RequestBody @Valid OnBoardingRequestDTO request
    ) {
        authService.addUserInfo(userId, request);
    }

    @GetMapping("/oauth/kakao/callback")
    public LoginUserInfoDto kakaoCallback(
            @RequestParam("code") String code
    ) {
        return kakaoAuthService.getKakaoUserInfoByCode(code);
    }
}
