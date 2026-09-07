package com.readora.backend.service;

import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.request.UpdateListeningProgressRequest;
import com.readora.backend.dto.response.ListeningProgressResponse;
import com.readora.backend.entity.Book;
import com.readora.backend.entity.ListeningProgress;
import com.readora.backend.entity.User;
import com.readora.backend.enums.BookStatus;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.exception.ResourceNotFoundException;
import com.readora.backend.mapper.ListeningProgressMapper;
import com.readora.backend.repository.ListeningProgressRepository;
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
public class ListeningProgressService {

    private final ListeningProgressRepository listeningProgressRepository;
    private final UserRepository userRepository;
    private final BookAccessService bookAccessService;

    @Transactional
    public ListeningProgressResponse getProgress(String email, Long bookId) {

        User user = getUserByEmail(email);

        Book book = bookAccessService.getAccessibleAudioBook(email, bookId);

        return listeningProgressRepository.findByUser_IdAndBook_Id(user.getId(), bookId)
                .map(ListeningProgressMapper::toResponse)
                .orElseGet(() -> ListeningProgressMapper.toDefaultResponse(book));
    }

    @Transactional
    public ListeningProgressResponse updateProgress(String email, Long bookId, UpdateListeningProgressRequest request) {

        User user = getUserByEmail(email);

        Book book = bookAccessService.getAccessibleAudioBook(email, bookId);

        validateSeconds(request);

        ListeningProgress progress = listeningProgressRepository.findByUser_IdAndBook_Id(user.getId(), bookId)
                .orElseGet(() -> createNewProgress(user, book));

        progress.setCurrentSeconds(request.currentSeconds());

        progress.setDurationSeconds(request.durationSeconds());

        progress.setCompleted(request.currentSeconds().equals(request.durationSeconds()));

        progress.setLastAccessedAt(LocalDateTime.now());

        ListeningProgress savedProgress = listeningProgressRepository.saveAndFlush(progress);

        return ListeningProgressMapper.toResponse(savedProgress);
    }

    @Transactional(readOnly = true)
    public PageResponse<ListeningProgressResponse> getContinueListening(String email, int page, int size) {

        validatePagination(page, size);

        User user = getUserByEmail(email);

        Pageable pageable = PageRequest.of(page, size);

        Page<ListeningProgress> progressPage = listeningProgressRepository
                .findAllByUser_IdAndCompletedFalseAndBook_StatusOrderByLastAccessedAtDesc(user.getId(),
                        BookStatus.PUBLISHED, pageable);

        Page<ListeningProgressResponse> responsePage = progressPage.map(ListeningProgressMapper::toResponse);

        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<ListeningProgressResponse> getRecentlyListened(String email, int page, int size) {

        validatePagination(page, size);

        User user = getUserByEmail(email);

        Pageable pageable = PageRequest.of(page, size);

        Page<ListeningProgress> progressPage = listeningProgressRepository
                .findAllByUser_IdAndBook_StatusOrderByLastAccessedAtDesc(user.getId(), BookStatus.PUBLISHED, pageable);

        Page<ListeningProgressResponse> responsePage = progressPage.map(ListeningProgressMapper::toResponse);

        return PageResponse.from(responsePage);
    }

    private ListeningProgress createNewProgress(User user, Book book) {

        return ListeningProgress.builder().user(user).book(book).currentSeconds(0).durationSeconds(null)
                .completed(false).build();
    }

    private void validateSeconds(UpdateListeningProgressRequest request) {

        if (request.currentSeconds() > request.durationSeconds()) {

            throw new BadRequestException("Current seconds cannot exceed duration seconds");
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
