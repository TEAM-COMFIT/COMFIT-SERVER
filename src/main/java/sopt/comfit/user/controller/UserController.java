package sopt.comfit.user.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.user.dto.response.GetMeResponseDto;
import sopt.comfit.user.service.UserService;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @SecurityRequirement(name = "JWT")
    public GetMeResponseDto getMe (@LoginUser Long userId){
        return userService.getMe(userId);
    }
}
