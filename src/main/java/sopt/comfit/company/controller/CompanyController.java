package sopt.comfit.company.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sopt.comfit.company.dto.response.GetCompanyResponseDto;
import sopt.comfit.company.service.CompanyService;
import sopt.comfit.global.annotation.LoginUser;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("{companyId}")
    public GetCompanyResponseDto getCompany(@LoginUser(required = false) Long userId ,
                                            @PathVariable Long companyId){
        if(userId == null) {
            return companyService.getPublicCompany(companyId);
        }
        return companyService.getCompany(userId ,companyId);

    }
}
