package sopt.comfit.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sopt.comfit.company.dto.response.GetReportCompanyResponseDto;
import sopt.comfit.experience.dto.response.GetReportExperienceResponseDto;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.global.dto.CommonApiResponse;
import sopt.comfit.global.dto.CustomErrorResponse;
import sopt.comfit.global.dto.PageDto;
import sopt.comfit.report.dto.request.MatchExperienceRequestDto;
import sopt.comfit.report.dto.response.AIReportResponseDto;
import sopt.comfit.report.dto.response.GetReportSummaryResponseDto;

@Tag(name = "AI-Report", description = "AI-Report 관련 API")
public interface AIReportSwagger {

    @Operation(
            summary = "AI 리포트 생성 API",
            description = "AI 리포트 생성 API입니다"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "AI 응답 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonApiResponse.class))),

            @ApiResponse(responseCode = "403", description = "권한 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "헤더값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자/기업/경험 id 값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "응답 파싱 및 AI 호출 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "JWT")
    AIReportResponseDto matchExperience(@LoginUser Long userId,
                                        @Valid @RequestBody MatchExperienceRequestDto requestDto);

    @Operation(
            summary = "AI_Report 리스트 조회/ 검색",
            description = "AI_Report 리스트 조회/검색 API입니다, " +
                    "KeyWord 값은 선택입니다"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI Report 리스트 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonApiResponse.class))),

            @ApiResponse(responseCode = "403", description = "권한 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "헤더값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @GetMapping
    @SecurityRequirement(name = "JWT")
    PageDto<GetReportSummaryResponseDto> getReportList(@LoginUser Long userId,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(required = false) String keyword);
    @Operation(
            summary = "Report 단일 조회",
            description = "Report 단일 조회 API입니다"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI Report 단일 조회",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonApiResponse.class))),

            @ApiResponse(responseCode = "403", description = "권한 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "헤더값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "리포트 id 값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @GetMapping("/{reportId}")
    @SecurityRequirement(name = "JWT")
    AIReportResponseDto getReport(@LoginUser Long userId,
                                  @PathVariable Long reportId);

    @Operation(
            summary = "Report 분석 전 경험 리스트 조회",
            description = "Report 분석 전 경험 리스트 조회 API입니다"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리스트 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonApiResponse.class))),

            @ApiResponse(responseCode = "403", description = "권한 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "헤더값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @GetMapping("/experiences")
    @SecurityRequirement(name = "JWT")
    GetReportExperienceResponseDto getReportExperience(@LoginUser Long userId);


    @Operation(
            summary = "Report 분석 전 기업 정보 단일 조회",
            description = "Report 분석 전 기업 정보 단일 조회"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "기업 단일 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonApiResponse.class))),

            @ApiResponse(responseCode = "403", description = "권한 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "헤더값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "company id 값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @GetMapping("/companies/{companyId}")
    @SecurityRequirement(name = "JWT")
    GetReportCompanyResponseDto getReportCompany(@PathVariable Long companyId);
}
