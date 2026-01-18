package sopt.comfit.company.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import sopt.comfit.company.domain.*;
import sopt.comfit.company.dto.response.*;
import sopt.comfit.company.exception.CompanyErrorCode;
import sopt.comfit.global.dto.PageDto;
import sopt.comfit.global.enums.EIndustry;
import sopt.comfit.global.enums.ESort;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.user.domain.CompanyLikeCount;
import sopt.comfit.user.domain.User;
import sopt.comfit.user.domain.UserCompanyRepository;
import sopt.comfit.user.domain.UserRepository;
import sopt.comfit.user.exception.UserErrorCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final CompanyIssueRepository companyIssueRepository;
    private final UserRepository userRepository;

    public CompanySearchListResponseDto searchCompanies(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new BaseException(CompanyErrorCode.INVALID_KEYWORD);
        }

        List<CompanySearchResponseDto> searchList = companyRepository.findByNameContaining(keyword)
                .stream()
                .map(CompanySearchResponseDto::from)
                .collect(Collectors.toList());

        return CompanySearchListResponseDto.from(searchList);
    }

    public PageDto<CompanyListResponseDto> getCompanyList(String keyword, String industry, String scale, String sort, Pageable pageable, Boolean isRecruited) {
        List<Company> companies = companyRepository.findAll();

        // 필터링
        if (keyword != null && !keyword.isEmpty()) {
            companies = companies.stream()
                    .filter(c -> c.getName().contains(keyword))
                    .collect(Collectors.toList());
        }
        if (industry != null && !industry.isEmpty()) {
            EIndustry industryEnum = EIndustry.from(industry);
            companies = companies.stream()
                    .filter(c -> c.getIndustry() == industryEnum)
                    .collect(Collectors.toList());
        }
        if (scale != null && !scale.isEmpty()) {
            EScale scaleEnum = EScale.valueOf(scale);
            companies = companies.stream()
                    .filter(c -> c.getScale() == scaleEnum)
                    .collect(Collectors.toList());
        }
        if (isRecruited != null) {
            companies = companies.stream()
                    .filter(c -> c.isRecruiting() == isRecruited)
                    .collect(Collectors.toList());
        }

        // 정렬
        if (sort != null && !sort.isEmpty()) {
            ESort sortEnum = ESort.valueOf(sort);
            companies = switch (sortEnum) {
                case NAME -> companies.stream()
                        .sorted((a, b) -> a.getName().compareTo(b.getName()))
                        .collect(Collectors.toList());
                case LATEST -> companies.stream()
                        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                        .collect(Collectors.toList());
                case OLDEST -> companies.stream()
                        .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                        .collect(Collectors.toList());
                case LIKE -> {
                    // LIKE 정렬은 likeCounts를 조회한 후 정렬해야 함
                    List<Long> companyIds = companies.stream()
                            .map(Company::getId)
                            .collect(Collectors.toList());
                    Map<Long, Long> likeCountsMap = userCompanyRepository.countByCompanyIds(companyIds)
                            .stream()
                            .collect(Collectors.toMap(
                                    CompanyLikeCount::getCompanyId,
                                    CompanyLikeCount::getLikeCount
                            ));
                    yield companies.stream()
                            .sorted((a, b) -> Long.compare(
                                    likeCountsMap.getOrDefault(b.getId(), 0L),
                                    likeCountsMap.getOrDefault(a.getId(), 0L)
                            ))
                            .collect(Collectors.toList());
                }
            };
        }

        // 페이지네이션
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), companies.size());
        List<Company> pagedCompanies = companies.subList(Math.min(start, companies.size()), end);

        // likeCounts 조회
        List<Long> companyIds = pagedCompanies.stream()
                .map(Company::getId)
                .collect(Collectors.toList());
        Map<Long, Long> likeCountsMap = userCompanyRepository.countByCompanyIds(companyIds)
                .stream()
                .collect(Collectors.toMap(
                        CompanyLikeCount::getCompanyId,
                        CompanyLikeCount::getLikeCount
                ));

        List<CompanyListResponseDto> content = pagedCompanies.stream()
                .map(company -> CompanyListResponseDto.from(
                        company,
                        likeCountsMap.getOrDefault(company.getId(), 0L)
                ))
                .collect(Collectors.toList());

        // PageDto 생성
        int totalPages = (int) Math.ceil((double) companies.size() / pageable.getPageSize());
        return new PageDto<>(
                content,
                pageable.getPageNumber() + 1,
                totalPages,
                companies.size()
        );
    }

    public List<FeaturedCompanyResponseDto> getFeaturedCompaniesWithoutUser() {
        List<Long> ids = companyRepository.findAllIds();
        return getFeaturedCompany(ids);
    }

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
