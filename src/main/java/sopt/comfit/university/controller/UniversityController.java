package sopt.comfit.university.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sopt.comfit.university.dto.response.SearchUniversityResponseDto;
import sopt.comfit.university.service.UniversityService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/universities")
public class UniversityController implements UniversitySwagger{

    private final UniversityService universityService;

    @Override
    public List<SearchUniversityResponseDto> searchUniversities(
            @RequestParam String keyword
    ) {
        return universityService.searchUniversities(keyword);
    }
}
