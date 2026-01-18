package sopt.comfit.university.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UniversityRepository extends JpaRepository<University, Long> {
    @Query("SELECT u FROM University u " +
            "WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY CASE WHEN LOWER(u.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 0 ELSE 1 END, " +
            "LENGTH(u.name) ASC")
    List<University> searchByKeyword(@Param("keyword") String keyword);
}
