package sopt.comfit.auth.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sopt.comfit.auth.dto.command.LoginCommandDto;
import sopt.comfit.auth.dto.request.LoginRequestDto;
import sopt.comfit.auth.service.AuthService;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.global.dto.JwtDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;

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
}
