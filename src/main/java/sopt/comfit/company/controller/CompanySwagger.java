package sopt.comfit.company.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import sopt.comfit.company.dto.response.*;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.global.dto.CommonApiResponse;
import sopt.comfit.global.dto.CustomErrorResponse;
import sopt.comfit.global.dto.PageDto;

import java.util.List;

@Tag(name = "Company", description = "기업 관련 API")
public interface CompanySwagger {


    @Operation(
            summary = "기업 검색/조회 API",
            description = "기업검색/조회 API 입니다"
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
    @GetMapping
    @SecurityRequirement(name = "JWT")
    PageDto<GetCompanyListResponseDto> getCompanyList(@RequestParam(required = false) String keyword,
                                                                  @RequestParam(required = false) String industry,
                                                                  @RequestParam(required = false) String scale,
                                                                  @RequestParam(required = false) String sort,
                                                                  @RequestParam(defaultValue = "1") int page,
                                                                  @RequestParam(required = false) Boolean isRecruited);
    @Operation(
            summary = "기업 검색 API",
            description = "기업검색 API 입니다"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "기업 검색 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonApiResponse.class))),

            @ApiResponse(responseCode = "403", description = "권한 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "헤더값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @GetMapping("/search")
    @SecurityRequirement(name= "JWT")
    List<GetCompanySearchResponseDto> getCompanySearchList(@RequestParam String keyword);


    @Operation(
            summary = "주요 기업 조회 API",
            description = "주요 기업 조회 API입니다. 토큰이 없으면 랜덤 3개, 토큰이 있으면 rank에 따라 사용자의 관심 산업군 기업을 반환합니다."
    )
    @SecurityRequirement(name = "JWT")
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
    @GetMapping("/major")
    List<FeaturedCompanyResponseDto> getFeaturedCompanies(@LoginUser Long userId,
                                                          @RequestParam int rank);

    @Operation(
            summary = "기업 상세 정보 조회 API",
            description = "기업 상세 정보 조회 API입니다"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonApiResponse.class))),

            @ApiResponse(responseCode = "403", description = "권한 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "헤더값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "기업 id 값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @GetMapping("{companyId}")
    @SecurityRequirement(name = "JWT")
    GetCompanyResponseDto getCompany(@LoginUser(required = false) Long userId ,
                                     @PathVariable Long companyId);

    @Operation(
            summary = "추천기업 조회 API",
            description = "추천 기업 조회 API입니다"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천 기업 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonApiResponse.class))),

            @ApiResponse(responseCode = "404", description = "기업 id 값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @GetMapping("{companyId}/suggestion")
    List<GetSuggestionCompanyResponseDto> getSuggestionCompany(@PathVariable Long companyId);

}
