package sopt.comfit.company.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;
import sopt.comfit.company.dto.response.FeaturedCompanyResponseDto;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.global.dto.CommonApiResponse;
import sopt.comfit.global.dto.CustomErrorResponse;

import java.util.List;

@Tag(name = "company", description = "기업 관련 API")
public interface CompanySwagger {

    @Operation(
            summary = "기업 검색/조회 API",
            description = "keyword만 있으면 기업 검색 API (id, name만 반환), 다른 필터가 있으면 일반 기업 조회 API입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "기업 검색/조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonApiResponse.class))),

            @ApiResponse(responseCode = "403", description = "권한 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "헤더값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "올바르지 않은 값입니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @org.springframework.web.bind.annotation.GetMapping
    @SecurityRequirement(name = "JWT")
    Object getCompanyList(@LoginUser Long userId,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String industry,
                          @RequestParam(required = false) String scale,
                          @RequestParam(required = false) String sort,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(required = false) Boolean isRecruited);

    @Operation(
            summary = "주요 기업 조회 API",
            description = "주요 기업 조회 API입니다. 토큰이 없으면 랜덤 3개, 토큰이 있으면 rank에 따라 사용자의 관심 산업군 기업을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주요 기업 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonApiResponse.class))),

            @ApiResponse(responseCode = "403", description = "권한 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "헤더값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "올바르지 않은 값입니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @org.springframework.web.bind.annotation.GetMapping("/major")
    List<FeaturedCompanyResponseDto> getFeaturedCompanies(@LoginUser Long userId,
                                                           @RequestParam int rank);
}
