package sopt.comfit.company.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sopt.comfit.global.enums.EIndustry;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByNameContaining(String keyword);

    @Query("SELECT c.id FROM Company c WHERE c.industry = :industry")
    List<Long> findIdsByIndustry(@Param("industry") EIndustry industry);

    List<Company> findByIndustryAndIdNot(EIndustry industry, Long id);

    @Query("SELECT c.id FROM Company c")
    List<Long> findAllIds();

}
