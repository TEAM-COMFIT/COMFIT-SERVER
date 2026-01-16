package sopt.comfit.company.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.comfit.company.domain.Company;
import sopt.comfit.company.domain.CompanyIssue;
import sopt.comfit.company.domain.CompanyIssueRepository;
import sopt.comfit.company.domain.CompanyRepository;
import sopt.comfit.company.dto.response.GetCompanyResponseDto;
import sopt.comfit.company.exception.CompanyErrorCode;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.user.domain.UserCompanyRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final CompanyIssueRepository companyIssueRepository;

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
}
