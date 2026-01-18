package sopt.comfit.company.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sopt.comfit.company.dto.response.GetCompanyListResponseDto;
import sopt.comfit.global.enums.EIndustry;
import sopt.comfit.global.enums.ESort;

public interface CompanyRepositoryCustom {

    Page<GetCompanyListResponseDto> getCompanyList(
            String keyword,
            EIndustry industry,
            EScale scale,
            Boolean isRecruited,
            ESort sort,
            Pageable pageable
    );

}
