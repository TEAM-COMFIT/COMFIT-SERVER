package sopt.comfit.user.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping("/companies/{companyId}")
    @SecurityRequirement(name = "JWT")
    @ResponseStatus(HttpStatus.CREATED)
    public Long addBookmark(@LoginUser Long userId,
                            @PathVariable Long companyId){
        return userService.addBookmark(userId, companyId);
    }

    @DeleteMapping("/companies/{companyId}")
    @SecurityRequirement(name = "JWT")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBookmark(@LoginUser Long userId,
                               @PathVariable Long companyId) {
        userService.removeBookmark(userId, companyId);
    }

}
