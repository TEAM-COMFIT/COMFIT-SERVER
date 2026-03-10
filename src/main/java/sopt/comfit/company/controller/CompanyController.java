package sopt.comfit.company.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sopt.comfit.company.domain.EScale;
import sopt.comfit.company.dto.response.*;
import sopt.comfit.company.service.CompanyService;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.global.dto.PageDto;
import sopt.comfit.global.enums.EIndustry;
import sopt.comfit.global.enums.ESort;

import java.util.List;
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController implements CompanySwagger {

    private final CompanyService companyService;


    @Override
    public PageDto<GetCompanyListResponseDto> getCompanyList(@RequestParam(required = false) String keyword,
                                                             @RequestParam(required = false) List<String> industry,
                                                             @RequestParam(required = false) List<String> scale,
                                                             @RequestParam(required = false) String sort,
                                                             @RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(required = false) Boolean isRecruited) {

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), 8);
        List<EIndustry> industryEnums = industry != null
                ? industry.stream().map(EIndustry::from).toList()
                : null;

        List<EScale> scaleEnums = scale != null
                ? scale.stream().map(EScale::from).toList()
                : null;

        ESort sortEnum = sort != null ? ESort.valueOf(sort) : null;


        return companyService.getCompanyList(keyword, industryEnums, scaleEnums, sortEnum, isRecruited, pageable);
    }

    @Override
    public List<GetCompanySearchResponseDto> getCompanySearchList(@RequestParam String keyword){

        return companyService.getCompanySearchList(keyword);
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
