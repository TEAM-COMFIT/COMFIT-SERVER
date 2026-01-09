package sopt.comfit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.global.dto.PageDto;
import sopt.comfit.global.enums.ESort;
import sopt.comfit.user.dto.response.GetBookmarkCompany;
import sopt.comfit.user.dto.response.GetMeResponseDto;
import sopt.comfit.user.service.UserService;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class UserController implements UserSwagger {

    private final UserService userService;

    @Override
    public GetMeResponseDto getMe (@LoginUser Long userId){
        return userService.getMe(userId);
    }

    @Override
    public Long addBookmark(@LoginUser Long userId,
                            @PathVariable Long companyId){
        return userService.addBookmark(userId, companyId);
    }

    @Override
    public void removeBookmark(@LoginUser Long userId,
                               @PathVariable Long companyId) {
        userService.removeBookmark(userId, companyId);
    }

    @Override
    public PageDto<GetBookmarkCompany> getBookmarkCompany(@LoginUser Long userId,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "LATEST") ESort sort){
        Pageable pageable = PageRequest.of(Math.max(page - 1 , 0),  6);
        return userService.getBookmarkCompany(userId, sort, pageable);
    }

}
