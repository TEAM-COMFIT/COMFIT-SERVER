package sopt.comfit.university.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.comfit.university.domain.University;
import sopt.comfit.university.domain.UniversityRepository;
import sopt.comfit.university.dto.response.SearchUniversityResponseDto;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class UniversityService {

    private final UniversityRepository universityRepository;

    @Transactional(readOnly = true)
    public List<SearchUniversityResponseDto> searchUniversities(String keyword) {

        return universityRepository.searchByKeyword(keyword)
                .stream().map(SearchUniversityResponseDto::from)
                .toList();

    }
}
