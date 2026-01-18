package sopt.comfit.company.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import sopt.comfit.company.domain.*;
import sopt.comfit.company.dto.response.*;
import sopt.comfit.company.exception.CompanyErrorCode;
import sopt.comfit.global.dto.PageDto;
import sopt.comfit.global.enums.EIndustry;
import sopt.comfit.global.enums.ESort;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.user.domain.User;
import sopt.comfit.user.domain.UserCompanyRepository;
import sopt.comfit.user.domain.UserRepository;
import sopt.comfit.user.exception.UserErrorCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final CompanyIssueRepository companyIssueRepository;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public PageDto<GetCompanyListResponseDto> getCompanyList( String keyword,
                                                              EIndustry industry,
                                                              EScale scale,
                                                              ESort sort,
                                                              Boolean isRecruited,
                                                              Pageable pageable){

        return PageDto.from(companyRepository.getCompanyList(
                keyword, industry, scale, isRecruited, sort, pageable
        ));
    }

    @Transactional(readOnly = true)
    public List<CompanySearchResponseDto> getCompanySearchList(String keyword){

        return companyRepository.searchByKeyword(keyword).stream()
                .map(CompanySearchResponseDto::from)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<FeaturedCompanyResponseDto> getFeaturedCompaniesWithoutUser() {
        List<Long> ids = companyRepository.findAllIds();
        return getFeaturedCompany(ids);
    }

    @Transactional(readOnly = true)
    public List<FeaturedCompanyResponseDto> getFeaturedCompaniesWithUser(Long userId, int rank) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.type(UserErrorCode.USER_NOT_FOUND));

        EIndustry targetIndustry = user.getIndustryByRank(rank);

        return getFeaturedCompany(companyRepository.findIdsByIndustry(targetIndustry));
    }


    @Transactional(readOnly = true)
    public GetCompanyResponseDto getCompany(Long userId, Long companyId){
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> BaseException.type(CompanyErrorCode.COMPANY_NOT_FOUND));

        boolean isLiked = userCompanyRepository.existsByCompanyIdAndUserId(companyId, userId);

        List<CompanyIssue> companyIssueList = companyIssueRepository.findByCompanyId(companyId);

        return GetCompanyResponseDto.of(company, isLiked, companyIssueList);
    }

    @Transactional(readOnly = true)
    public GetCompanyResponseDto getPublicCompany(Long companyId){
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> BaseException.type(CompanyErrorCode.COMPANY_NOT_FOUND));

        List<CompanyIssue> companyIssueList = companyIssueRepository.findByCompanyId(companyId);

        return GetCompanyResponseDto.of(company, null, companyIssueList);
    }

    @Transactional(readOnly = true)
    public List<GetSuggestionCompanyResponseDto> getSuggestionCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> BaseException.type(CompanyErrorCode.COMPANY_NOT_FOUND));

        List<Company> candidates = new ArrayList<>(companyRepository
                .findByIndustryAndIdNot(company.getIndustry(), companyId));

        Collections.shuffle(candidates);

        return candidates.stream()
                .limit(4)
                .map(GetSuggestionCompanyResponseDto::from)
                .toList();
    }

    private List<FeaturedCompanyResponseDto> getFeaturedCompany(List<Long> ids){
        Collections.shuffle(ids);
        List<Long> randomIds = ids.subList(0, Math.min(3, ids.size()));

        return companyRepository.findAllById(randomIds).stream()
                .map(FeaturedCompanyResponseDto::from)
                .collect(Collectors.toList());
    }
}
