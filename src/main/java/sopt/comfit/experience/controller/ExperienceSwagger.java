package sopt.comfit.experience.controller;

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
import sopt.comfit.experience.domain.EType;
import sopt.comfit.experience.dto.request.ExperienceRequestDto;
import sopt.comfit.experience.dto.response.GetExperienceResponseDto;
import sopt.comfit.experience.dto.response.GetSummaryExperienceResponseDto;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.global.dto.CommonApiResponse;
import sopt.comfit.global.dto.CustomErrorResponse;
import sopt.comfit.global.dto.PageDto;

@Tag(name = "experience", description = "경험 관련 API")
public interface ExperienceSwagger {

    @Operation(
            summary = "경험 생성 API",
            description = "경험을 생성하는 API입니다"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "경험 생성 성공",
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
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "JWT")
    Long createExperience(@LoginUser Long userId,
                          @Valid @RequestBody ExperienceRequestDto request);



    @Operation(
            summary = "경험 요약 리스트 조회 API",
            description = "경험 요약 리스트 조회 API입니다"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리스트 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageDto.class))),

            @ApiResponse(responseCode = "403", description = "권한 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "헤더값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
    })
    @GetMapping
    @SecurityRequirement(name = "JWT")
    PageDto<GetSummaryExperienceResponseDto> getSummaryExperienceList(@LoginUser Long userId,
                                                                      @RequestParam(required = false) EType type,
                                                                      @RequestParam(defaultValue = "0") int page);

    @Operation(
            summary = "경험 세부 조회 API",
            description = "경험 세부를 조회하는 API입니다"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경험 세부 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonApiResponse.class))),

            @ApiResponse(responseCode = "403", description = "권한 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "헤더값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "없는 경험 조회",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @GetMapping("/{experienceId}")
    @SecurityRequirement(name="JWT")
    GetExperienceResponseDto getExperience(@LoginUser Long userId,
                                           @PathVariable Long experienceId);


    @Operation(
            summary = "경험 수정 API",
            description = "경험을 수정하는 API입니다"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경험 수정 성공",
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
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "없는 경험 조회",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @PatchMapping("/{experienceId}")
    @SecurityRequirement(name = "JWT")
    Long updateExperience(@LoginUser Long userId,
                          @PathVariable Long experienceId,
                          @Valid @RequestBody ExperienceRequestDto request);


    @Operation(
            summary = "경험 삭제 API",
            description = "경험을 삭제하는 API입니다"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "경험 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "헤더값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "없는 경험 조회",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @DeleteMapping("/{experienceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "JWT")
    void deleteExperience(@LoginUser Long userId,
                          @PathVariable Long experienceId);

    @SecurityRequirement(name = "JWT")
    @PatchMapping("{experienceId}/default")
    public void updateDefault(@LoginUser Long userId,
                              @PathVariable Long experienceId);
}

