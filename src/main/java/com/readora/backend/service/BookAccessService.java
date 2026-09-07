package com.readora.backend.service;

import com.readora.backend.dto.response.BookMediaAccessResponse;
import com.readora.backend.entity.Book;
import com.readora.backend.entity.User;
import com.readora.backend.enums.BookAccessType;
import com.readora.backend.enums.BookStatus;
import com.readora.backend.exception.ForbiddenException;
import com.readora.backend.exception.ResourceNotFoundException;
import com.readora.backend.repository.BookRepository;
import com.readora.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookAccessService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    @Transactional
    public BookMediaAccessResponse getPdfAccess(String email, Long bookId) {

        Book book = getAccessiblePdfBook(email, bookId);

        return new BookMediaAccessResponse(book.getId(), book.getPdfUrl());
    }

    @Transactional
    public BookMediaAccessResponse getAudioAccess(String email, Long bookId) {

        Book book = getAccessibleAudioBook(email, bookId);

        return new BookMediaAccessResponse(book.getId(), book.getAudioUrl());
    }

    @Transactional
    public Book getAccessiblePdfBook(String email, Long bookId) {

        Book book = getAccessibleBook(email, bookId);

        if (book.getPdfUrl() == null || book.getPdfUrl().isBlank()) {

            throw new ResourceNotFoundException("PDF not found for this book");
        }

        return book;
    }

    @Transactional
    public Book getAccessibleAudioBook(String email, Long bookId) {

        Book book = getAccessibleBook(email, bookId);

        if (book.getAudioUrl() == null || book.getAudioUrl().isBlank()) {

            throw new ResourceNotFoundException("Audio not found for this book");
        }

        return book;
    }

    private Book getAccessibleBook(String email, Long bookId) {

        User user = getUserByEmail(email);

        Book book = bookRepository.findById(bookId).orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        validatePublishedBook(book);
        validateBookAccess(user, book);

        return book;
    }

    private void validatePublishedBook(Book book) {

        if (book.getStatus() != BookStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Book not found");
        }
    }

    private void validateBookAccess(User user, Book book) {

        if (book.getAccessType() == BookAccessType.FREE) {

            return;
        }

        boolean hasPremiumAccess = subscriptionService.hasActiveSubscription(user.getId());

        if (!hasPremiumAccess) {
            throw new ForbiddenException("Premium subscription is required to access this book");
        }
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}