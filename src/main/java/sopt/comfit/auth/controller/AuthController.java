package sopt.comfit.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sopt.comfit.auth.dto.LoginResponseDto;
import sopt.comfit.auth.dto.ReIssueTokenResponseDto;
import sopt.comfit.auth.dto.command.LoginCommandDto;
import sopt.comfit.auth.dto.query.LoginQueryDto;
import sopt.comfit.auth.dto.request.LoginRequestDto;
import sopt.comfit.auth.dto.request.OnBoardingRequestDTO;
import sopt.comfit.auth.dto.request.ReIssueTokenRequestDto;
import sopt.comfit.auth.kakao.service.KakaoAuthService;
import sopt.comfit.auth.service.AuthService;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.global.dto.JwtDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController implements AuthSwagger{

    private final AuthService authService;
    private final KakaoAuthService kakaoAuthService;

    @PostMapping("/login")
    public JwtDto join(
            @RequestBody @Valid LoginRequestDto request
    ){
        return authService.login(LoginCommandDto.from(request));
    }

    @Override
    public void logout(
            @LoginUser Long userId
    ){
        authService.logout(userId);
    }

    @Override
    public ReIssueTokenResponseDto reissueToken(
            @RequestBody @Valid ReIssueTokenRequestDto request
    ) {
        return authService.reissueToken(request.refreshToken());
    }

    @Override
    public void addUserInfo(
            @LoginUser Long userId,
            @RequestBody @Valid OnBoardingRequestDTO request
    ) {
        authService.addUserInfo(userId, request);
    }

    @Override
    public LoginResponseDto kakaoCallback(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) {
        LoginQueryDto loginQueryDto = kakaoAuthService.getKakaoUserInfoByCode(code);
        response.addCookie(new Cookie("refreshToken", loginQueryDto.jwtDto().refreshToken()));
        return LoginResponseDto.of(loginQueryDto);
    }
}
