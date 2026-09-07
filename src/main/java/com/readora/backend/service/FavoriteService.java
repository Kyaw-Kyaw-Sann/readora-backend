package com.readora.backend.service;

import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.response.BookPublicSummaryResponse;
import com.readora.backend.dto.response.FavoriteStatusResponse;
import com.readora.backend.entity.Book;
import com.readora.backend.entity.Favorite;
import com.readora.backend.entity.User;
import com.readora.backend.enums.BookStatus;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.exception.ResourceNotFoundException;
import com.readora.backend.mapper.BookMapper;
import com.readora.backend.repository.BookRepository;
import com.readora.backend.repository.FavoriteRepository;
import com.readora.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Transactional
    public BookPublicSummaryResponse addFavorite(String email, Long bookId) {

        User user = getUserByEmail(email);
        Book book = getPublishedBook(bookId);

        boolean alreadyFavorite = favoriteRepository.existsByUser_IdAndBook_Id(user.getId(), book.getId());

        if (alreadyFavorite) {
            throw new BadRequestException("Book is already in favorites");
        }

        Favorite favorite = Favorite.builder().user(user).book(book).build();

        favoriteRepository.save(favorite);

        return BookMapper.toPublicSummaryResponse(book);
    }

    @Transactional
    public void removeFavorite(String email, Long bookId) {

        User user = getUserByEmail(email);

        Favorite favorite = favoriteRepository.findByUser_IdAndBook_Id(user.getId(), bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));

        favoriteRepository.delete(favorite);
    }

    @Transactional(readOnly = true)
    public FavoriteStatusResponse getFavoriteStatus(String email, Long bookId) {

        User user = getUserByEmail(email);

        getPublishedBook(bookId);

        boolean favorite = favoriteRepository.existsByUser_IdAndBook_Id(user.getId(), bookId);

        return new FavoriteStatusResponse(favorite);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookPublicSummaryResponse> getFavorites(String email, int page, int size) {

        validatePagination(page, size);

        User user = getUserByEmail(email);

        Pageable pageable = PageRequest.of(page, size);

        Page<Favorite> favoritePage = favoriteRepository.findAllByUser_IdOrderByCreatedAtDesc(user.getId(), pageable);

        Page<BookPublicSummaryResponse> responsePage = favoritePage
                .map(favorite -> BookMapper.toPublicSummaryResponse(favorite.getBook()));

        return PageResponse.from(responsePage);
    }

    private Book getPublishedBook(Long bookId) {

        Book book = bookRepository.findById(bookId).orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (book.getStatus() != BookStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Book not found");
        }

        return book;
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