package sopt.comfit.company.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import sopt.comfit.company.dto.response.FeaturedCompanyResponseDto;
import sopt.comfit.company.service.CompanyService;
import sopt.comfit.global.annotation.LoginUser;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController implements CompanySwagger {

    private final CompanyService companyService;

    @Override
    public Object getCompanyList(@LoginUser Long userId,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String industry,
                                  @RequestParam(required = false) String scale,
                                  @RequestParam(required = false) String sort,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(required = false) Boolean isRecruited) {
        // keyword만 있고 다른 필터가 없으면 검색 API
        if (keyword != null && industry == null && scale == null && sort == null && isRecruited == null) {
            return companyService.searchCompanies(keyword);
        }
        // 그 외에는 일반 기업 조회 API
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), 8);
        return companyService.getCompanyList(keyword, industry, scale, sort, pageable, isRecruited);
    }

    @Override
    public List<FeaturedCompanyResponseDto> getFeaturedCompanies(@LoginUser(nullable = true) Long userId,
                                                                  @RequestParam int rank) {
        if (userId == null) {
            return companyService.getFeaturedCompaniesWithoutUser(rank);
        }
        return companyService.getFeaturedCompaniesWithUser(userId, rank);
    }
}
