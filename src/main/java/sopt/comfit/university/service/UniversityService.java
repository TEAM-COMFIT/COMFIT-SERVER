package sopt.comfit.university.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.comfit.university.domain.University;
import sopt.comfit.university.domain.UniversityRepository;
import sopt.comfit.university.dto.response.UniversityItemDto;
import sopt.comfit.university.dto.response.UniversitySearchResponseDto;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class UniversityService {

    private final UniversityRepository universityRepository;

    @Transactional(readOnly = true)
    public UniversitySearchResponseDto searchUniversities(String keyword) {
        List<University> universities = universityRepository.findByNameContainingIgnoreCase(keyword);
        
        List<UniversityItemDto> universityList = universities.stream()
                .map(UniversityItemDto::from)
                .toList();

        return UniversitySearchResponseDto.from(universityList);
    }
}
