package sopt.comfit.company.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import sopt.comfit.company.domain.EScale;
import sopt.comfit.company.dto.response.FeaturedCompanyResponseDto;
import sopt.comfit.company.dto.response.GetCompanyListResponseDto;
import sopt.comfit.company.dto.response.GetCompanyResponseDto;
import sopt.comfit.company.dto.response.GetSuggestionCompanyResponseDto;
import sopt.comfit.company.service.CompanyService;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.global.dto.PageDto;
import sopt.comfit.global.enums.EIndustry;
import sopt.comfit.global.enums.ESort;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController implements CompanySwagger {

    private final CompanyService companyService;


    @Override
    public PageDto<GetCompanyListResponseDto> getCompanyList(@RequestParam(required = false) String keyword,
                                                             @RequestParam(required = false) String industry,
                                                             @RequestParam(required = false) String scale,
                                                             @RequestParam(required = false) String sort,
                                                             @RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(required = false) Boolean isRecruited) {

        Pageable pageable = PageRequest.of(Math.min(page - 1, 0), 8);
        EIndustry industryEnum = industry != null ? EIndustry.from(industry) : null;
        EScale scaleEnum = scale != null ? EScale.valueOf(scale) : null;
        ESort sortEnum = sort != null ? ESort.valueOf(sort) : null;


        return companyService.getCompanyList(keyword, industryEnum, scaleEnum, sortEnum, isRecruited, pageable);
    }



    @Override
    public List<FeaturedCompanyResponseDto> getFeaturedCompanies(@LoginUser(required = false) Long userId,
                                                                 @RequestParam int rank) {
        if (userId == null) {
            return companyService.getFeaturedCompaniesWithoutUser();
        }
        return companyService.getFeaturedCompaniesWithUser(userId, rank);
    }

    @Override
    public GetCompanyResponseDto getCompany(@LoginUser(required = false) Long userId ,
                                            @PathVariable Long companyId){
        if(userId == null) {
            return companyService.getPublicCompany(companyId);
        }

        return companyService.getCompany(userId ,companyId);

    }

    @Override
    public List<GetSuggestionCompanyResponseDto> getSuggestionCompany(@PathVariable Long companyId){
        return companyService.getSuggestionCompany(companyId);
    }
}
