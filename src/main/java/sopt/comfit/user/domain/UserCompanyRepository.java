package sopt.comfit.user.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserCompanyRepository extends JpaRepository<UserCompany, Long> {
    Optional<UserCompany> findByCompanyIdAndUserId(Long companyId, Long userId);

    Page<UserCompany> findByUserIdOrderByCreatedAtAsc(Long userId, Pageable pageable);

    Page<UserCompany> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<UserCompany> findByUserIdOrderByCompanyName(Long companyId, Pageable pageable);

    boolean existsByCompanyIdAndUserId(Long companyId, Long userId);

    @Query("SELECT uc.company.id as companyId, COUNT(uc) as likeCount FROM UserCompany uc WHERE uc.company.id IN :companyIds GROUP BY uc.company.id")
    List<CompanyLikeCount> countByCompanyIds(@Param("companyIds") List<Long> companyIds);
}
