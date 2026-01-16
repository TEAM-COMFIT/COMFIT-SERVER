package sopt.comfit.university.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.university.dto.response.UniversitySearchResponseDto;
import sopt.comfit.university.service.UniversityService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/universities")
@Tag(name = "대학교")
public class UniversityController {

    private final UniversityService universityService;

    @GetMapping("/search")
    @Operation(
            summary = "대학교 검색",
            description = "키워드로 대학교를 검색합니다."
    )
    @SecurityRequirement(name = "JWT")
    public UniversitySearchResponseDto searchUniversities(
            @LoginUser Long userId,
            @RequestParam String keyword
    ) {
        return universityService.searchUniversities(keyword);
    }
}
