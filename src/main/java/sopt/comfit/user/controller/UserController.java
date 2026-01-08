package sopt.comfit.user.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.global.dto.PageDto;
import sopt.comfit.global.enums.ESort;
import sopt.comfit.user.dto.response.GetBookmarkCompany;
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

    @GetMapping("/companies")
    @SecurityRequirement(name = "JWT")
    public PageDto<GetBookmarkCompany> getBookmarkCompany(@LoginUser Long userId,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "LATEST") ESort sort){
        Pageable pageable = PageRequest.of(page,  6);
        return userService.getBookmarkCompany(userId, sort, pageable);
    }

}
