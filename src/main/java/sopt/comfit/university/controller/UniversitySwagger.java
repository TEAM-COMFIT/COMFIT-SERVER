package sopt.comfit.university.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sopt.comfit.global.dto.CommonApiResponse;
import sopt.comfit.global.dto.CustomErrorResponse;
import sopt.comfit.university.dto.response.SearchUniversityResponseDto;

import java.util.List;

@Tag(name = "university", description = "대학 관련 API")
public interface UniversitySwagger {


    @Operation(
            summary = "대학교 검색",
            description = "키워드로 대학교를 검색합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "대학교 검색 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonApiResponse.class))),

            @ApiResponse(responseCode = "403", description = "권한 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "헤더값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @GetMapping("/search")
    @SecurityRequirement(name = "JWT")
    List<SearchUniversityResponseDto> searchUniversities(
            @RequestParam String keyword
    );
}
