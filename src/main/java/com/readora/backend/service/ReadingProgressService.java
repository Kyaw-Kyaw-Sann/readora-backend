package com.readora.backend.service;

import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.request.UpdateReadingProgressRequest;
import com.readora.backend.dto.response.ReadingProgressResponse;
import com.readora.backend.entity.Book;
import com.readora.backend.entity.ReadingProgress;
import com.readora.backend.entity.User;
import com.readora.backend.enums.BookStatus;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.exception.ResourceNotFoundException;
import com.readora.backend.mapper.ReadingProgressMapper;
import com.readora.backend.repository.ReadingProgressRepository;
import com.readora.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReadingProgressService {

    private final ReadingProgressRepository readingProgressRepository;
    private final UserRepository userRepository;
    private final BookAccessService bookAccessService;

    @Transactional
    public ReadingProgressResponse getProgress(String email, Long bookId) {

        User user = getUserByEmail(email);

        Book book = bookAccessService.getAccessiblePdfBook(email, bookId);

        return readingProgressRepository.findByUser_IdAndBook_Id(user.getId(), bookId)
                .map(ReadingProgressMapper::toResponse).orElseGet(() -> ReadingProgressMapper.toDefaultResponse(book));
    }

    @Transactional
    public ReadingProgressResponse updateProgress(String email, Long bookId, UpdateReadingProgressRequest request) {

        User user = getUserByEmail(email);

        Book book = bookAccessService.getAccessiblePdfBook(email, bookId);

        validatePages(request);

        ReadingProgress progress = readingProgressRepository.findByUser_IdAndBook_Id(user.getId(), bookId)
                .orElseGet(() -> createNewProgress(user, book));

        progress.setCurrentPage(request.currentPage());

        progress.setTotalPages(request.totalPages());

        progress.setCompleted(request.currentPage().equals(request.totalPages()));

        progress.setLastAccessedAt(LocalDateTime.now());

        ReadingProgress savedProgress = readingProgressRepository.saveAndFlush(progress);

        return ReadingProgressMapper.toResponse(savedProgress);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReadingProgressResponse> getContinueReading(String email, int page, int size) {

        validatePagination(page, size);

        User user = getUserByEmail(email);

        Pageable pageable = PageRequest.of(page, size);

        Page<ReadingProgress> progressPage = readingProgressRepository
                .findAllByUser_IdAndCompletedFalseAndBook_StatusOrderByLastAccessedAtDesc(user.getId(),
                        BookStatus.PUBLISHED, pageable);

        Page<ReadingProgressResponse> responsePage = progressPage.map(ReadingProgressMapper::toResponse);

        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReadingProgressResponse> getRecentlyRead(String email, int page, int size) {

        validatePagination(page, size);

        User user = getUserByEmail(email);

        Pageable pageable = PageRequest.of(page, size);

        Page<ReadingProgress> progressPage = readingProgressRepository
                .findAllByUser_IdAndBook_StatusOrderByLastAccessedAtDesc(user.getId(), BookStatus.PUBLISHED, pageable);

        Page<ReadingProgressResponse> responsePage = progressPage.map(ReadingProgressMapper::toResponse);

        return PageResponse.from(responsePage);
    }

    private ReadingProgress createNewProgress(User user, Book book) {

        return ReadingProgress.builder().user(user).book(book).currentPage(1).totalPages(null).completed(false).build();
    }

    private void validatePages(UpdateReadingProgressRequest request) {

        if (request.currentPage() > request.totalPages()) {

            throw new BadRequestException("Current page cannot exceed total pages");
        }
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validatePagination(int page, int size) {

        if (page < 0) {
            throw new BadRequestException("Page number must be 0 or greater");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }
    }
}
