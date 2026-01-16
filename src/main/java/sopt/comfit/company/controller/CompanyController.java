package sopt.comfit.company.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sopt.comfit.company.dto.response.GetCompanyResponseDto;
import sopt.comfit.company.dto.response.GetSuggestionCompanyResponseDto;
import sopt.comfit.company.service.CompanyService;
import sopt.comfit.global.annotation.LoginUser;

import java.util.List;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("{companyId}")
    @SecurityRequirement(name = "JWT")
    public GetCompanyResponseDto getCompany(@LoginUser(required = false) Long userId ,
                                            @PathVariable Long companyId){
        if(userId == null) {
            return companyService.getPublicCompany(companyId);
        }
        return companyService.getCompany(userId ,companyId);

    }

    @GetMapping("{companyId}/suggestion")
    public List<GetSuggestionCompanyResponseDto> getSuggestionCompany(@PathVariable Long companyId){
        return companyService.getSuggestionCompany(companyId);
    }
}
