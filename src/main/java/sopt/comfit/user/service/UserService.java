package sopt.comfit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.comfit.company.domain.Company;
import sopt.comfit.company.domain.CompanyRepository;
import sopt.comfit.company.exception.CompanyErrorCode;
import sopt.comfit.global.dto.PageDto;
import sopt.comfit.global.enums.ESort;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.global.exception.CommonErrorCode;
import sopt.comfit.report.domain.AIReportRepository;
import sopt.comfit.user.domain.User;
import sopt.comfit.user.domain.UserCompany;
import sopt.comfit.user.domain.UserCompanyRepository;
import sopt.comfit.user.domain.UserRepository;
import sopt.comfit.user.dto.response.GetBookmarkCompany;
import sopt.comfit.user.dto.response.GetMeResponseDto;
import sopt.comfit.user.exception.UserCompanyErrorCode;
import sopt.comfit.user.exception.UserErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final AIReportRepository aIReportRepository;

    @Transactional(readOnly = true)
    public GetMeResponseDto getMe (Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.type(UserErrorCode.USER_NOT_FOUND));

        return GetMeResponseDto.from(user);
    }

    @Transactional
    public Long addBookmark(Long userId, Long companyId) {

        log.info("북마크 추가 userId:{} companyId:{}", userId, companyId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.type(UserErrorCode.USER_NOT_FOUND));
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> BaseException.type(CompanyErrorCode.COMPANY_NOT_FOUND));

        boolean isConnected = aIReportRepository.existsByCompanyIdAndUserId(companyId, userId);
        UserCompany userCompany = userCompanyRepository.save(UserCompany.create(user, company, isConnected));
        return userCompany.getId();
    }

    @Transactional
    public void removeBookmark(Long userId, Long companyId) {
        log.info("북마크 삭제 userId:{} companyId:{}", userId, companyId);
        UserCompany userCompany = userCompanyRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> BaseException.type(UserCompanyErrorCode.USER_COMPANY_NOT_FOUND));

        userCompanyRepository.delete(userCompany);
    }

    @Transactional(readOnly = true)
    public PageDto<GetBookmarkCompany> getBookmarkCompany (Long userId, ESort sort, Pageable pageable) {

        Page<UserCompany> page = switch (sort) {
            case LATEST -> userCompanyRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
            case OLDEST -> userCompanyRepository.findByUserIdOrderByCreatedAtAsc(userId, pageable);
            case NAME -> userCompanyRepository.findByUserIdOrderByCompanyName(userId, pageable);
            default -> {
                log.warn("잘못된 정렬 타입 값입니다 type : {}", sort);
                throw BaseException.type(CommonErrorCode.NOT_SUPPORTED_SORT_TYPE);
            }
        };

        return PageDto.from(page.map(GetBookmarkCompany::from));
    }
}
