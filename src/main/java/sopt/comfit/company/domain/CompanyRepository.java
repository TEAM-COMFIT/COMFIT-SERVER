package sopt.comfit.company.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import sopt.comfit.global.enums.EIndustry;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findTop4ByIndustryAndIdNot(EIndustry industry, Long id);
}
