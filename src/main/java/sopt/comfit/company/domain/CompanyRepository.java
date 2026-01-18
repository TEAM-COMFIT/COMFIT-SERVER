package sopt.comfit.company.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sopt.comfit.global.enums.EIndustry;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long>, CompanyRepositoryCustom {

    @Query("SELECT c.id FROM Company c WHERE c.industry = :industry")
    List<Long> findIdsByIndustry(@Param("industry") EIndustry industry);

    List<Company> findByIndustryAndIdNot(EIndustry industry, Long id);

    @Query("SELECT c.id FROM Company c")
    List<Long> findAllIds();

    @Query("SELECT c FROM Company c " +
            "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY CASE WHEN LOWER(c.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 0 ELSE 1 END, " +
            "LENGTH(c.name) ASC")
    List<Company> searchByKeyword(@Param("keyword") String keyword);
}
