package sopt.comfit.company.domain;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import sopt.comfit.company.dto.response.GetCompanyListResponseDto;
import sopt.comfit.global.enums.EIndustry;
import sopt.comfit.global.enums.ESort;
import sopt.comfit.user.domain.CompanyLikeCount;
import sopt.comfit.user.domain.UserCompanyRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static sopt.comfit.company.domain.QCompany.company;

@RequiredArgsConstructor
public class CompanyRepositoryImpl implements CompanyRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final UserCompanyRepository userCompanyRepository;

    @Override
    public Page<GetCompanyListResponseDto> getCompanyList(
            String keyword,
            EIndustry industry,
            EScale scale,
            Boolean isRecruited,
            ESort sort,
            Pageable pageable
    ) {
        // 기본 쿼리 (필터링 + 정렬 + 페이징)
        List<Company> companies = queryFactory
                .selectFrom(company)
                .where(
                        keywordContains(keyword),
                        industryEq(industry),
                        scaleEq(scale),
                        recruitingEq(isRecruited)
                )
                .orderBy(getOrderSpecifier(sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // LIKE 정렬인 경우 별도 처리
        if (ESort.LIKE.equals(sort)) {
            companies = sortByLikeCount(keyword, industry, scale, isRecruited, pageable);
        }

        // likeCount 조회
        List<Long> companyIds = companies.stream()
                .map(Company::getId)
                .toList();

        Map<Long, Long> likeCountsMap = userCompanyRepository.countByCompanyIds(companyIds)
                .stream()
                .collect(Collectors.toMap(
                        CompanyLikeCount::getCompanyId,
                        CompanyLikeCount::getLikeCount
                ));

        // DTO 변환
        List<GetCompanyListResponseDto> content = companies.stream()
                .map(c -> GetCompanyListResponseDto.from(c, likeCountsMap.getOrDefault(c.getId(), 0L)))
                .toList();

        // count 쿼리 (지연 로딩)
        JPAQuery<Long> countQuery = queryFactory
                .select(company.count())
                .from(company)
                .where(
                        keywordContains(keyword),
                        industryEq(industry),
                        scaleEq(scale),
                        recruitingEq(isRecruited)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private List<Company> sortByLikeCount(
            String keyword,
            EIndustry industry,
            EScale scale,
            Boolean isRecruited,
            Pageable pageable
    ) {
        // 전체 필터링된 회사 ID 조회
        List<Long> filteredIds = queryFactory
                .select(company.id)
                .from(company)
                .where(
                        keywordContains(keyword),
                        industryEq(industry),
                        scaleEq(scale),
                        recruitingEq(isRecruited)
                )
                .fetch();

        // 좋아요 수 조회
        Map<Long, Long> likeCountsMap = userCompanyRepository.countByCompanyIds(filteredIds)
                .stream()
                .collect(Collectors.toMap(
                        CompanyLikeCount::getCompanyId,
                        CompanyLikeCount::getLikeCount
                ));

        // 좋아요 순 정렬 후 페이징
        List<Long> sortedIds = filteredIds.stream()
                .sorted((a, b) -> Long.compare(
                        likeCountsMap.getOrDefault(b, 0L),
                        likeCountsMap.getOrDefault(a, 0L)
                ))
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .toList();

        // ID 순서대로 Company 조회
        List<Company> companies = queryFactory
                .selectFrom(company)
                .where(company.id.in(sortedIds))
                .fetch();

        // 정렬 순서 유지
        Map<Long, Company> companyMap = companies.stream()
                .collect(Collectors.toMap(Company::getId, c -> c));

        return sortedIds.stream()
                .map(companyMap::get)
                .toList();
    }

    private BooleanExpression keywordContains(String keyword) {
        return keyword != null && !keyword.isEmpty()
                ? company.name.contains(keyword)
                : null;
    }

    private BooleanExpression industryEq(EIndustry industry) {
        return industry != null ? company.industry.eq(industry) : null;
    }

    private BooleanExpression scaleEq(EScale scale) {
        return scale != null ? company.scale.eq(scale) : null;
    }

    private BooleanExpression recruitingEq(Boolean isRecruited) {
        return isRecruited != null ? company.isRecruiting.eq(isRecruited) : null;
    }

    private OrderSpecifier<?> getOrderSpecifier(ESort sort) {
        if (sort == null) {
            return company.createdAt.desc(); // 기본 정렬
        }

        return switch (sort) {
            case NAME -> company.name.asc();
            case OLDEST -> company.createdAt.asc();
            default -> company.createdAt.desc();
        };
    }
}