package sopt.comfit.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sopt.comfit.auth.dto.AccessTokenResponseDto;
import sopt.comfit.auth.dto.LoginResponseDto;
import sopt.comfit.auth.dto.command.LoginCommandDto;
import sopt.comfit.auth.dto.command.OnBoardingCommandDto;
import sopt.comfit.auth.dto.query.LoginQueryDto;
import sopt.comfit.auth.dto.request.LoginRequestDto;
import sopt.comfit.auth.dto.request.OnBoardingRequestDTO;
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
            @RequestBody @Valid LoginRequestDto request,
            HttpServletResponse response
    ){
        JwtDto dto = authService.login(LoginCommandDto.from(request));

        response.addHeader("Set-Cookie",
                "refreshToken=" + dto.refreshToken() +
                        "; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=86400");

        return dto;
    }

    @Override
    public void logout(
            @LoginUser Long userId,
            HttpServletResponse response
    ){
        authService.logout(userId);

        response.addHeader("Set-Cookie",
                "refreshToken=; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=0");
    }

    @Override
    public AccessTokenResponseDto reissueToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        JwtDto dto = authService.reissueToken(refreshToken);

        response.addHeader("Set-Cookie",
                "refreshToken=" + dto.refreshToken() +
                        "; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=86400");

        return AccessTokenResponseDto.from(dto.accessToken());
    }

    @Override
    public void addUserInfo(
            @LoginUser Long userId,
            @RequestBody @Valid OnBoardingRequestDTO request
    ) {
        authService.addUserInfo(OnBoardingCommandDto.of(userId, request));
    }

    @Override
    public LoginResponseDto kakaoCallback(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) {
        LoginQueryDto loginQueryDto = kakaoAuthService.getKakaoUserInfoByCode(code);

        response.addHeader("Set-Cookie",
                "refreshToken=" + loginQueryDto.jwtDto().refreshToken() +
                        "; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=86400");

        return LoginResponseDto.of(loginQueryDto);
    }
}
