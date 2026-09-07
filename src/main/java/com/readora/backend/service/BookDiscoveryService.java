package com.readora.backend.service;

import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.response.BookPublicDetailResponse;
import com.readora.backend.dto.response.BookPublicSummaryResponse;
import com.readora.backend.entity.Book;
import com.readora.backend.enums.BookAccessType;
import com.readora.backend.enums.BookSort;
import com.readora.backend.enums.BookStatus;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.exception.ResourceNotFoundException;
import com.readora.backend.mapper.BookMapper;
import com.readora.backend.repository.BookRepository;
import com.readora.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookDiscoveryService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<BookPublicSummaryResponse> getBooks(String search, Long categoryId, BookAccessType accessType,
            BookSort sort, int page, int size) {

        validatePagination(page, size);
        validateCategory(categoryId);

        String normalizedSearch = normalizeText(search);

        Pageable pageable = PageRequest.of(page, size, getSort(sort));

        Page<Book> books;

        if (normalizedSearch == null) {

            books = bookRepository.findPublishedBooks(accessType, categoryId, pageable);

        } else {

            String searchPattern = "%" + normalizedSearch + "%";

            books = bookRepository.findPublishedBooksBySearch(searchPattern, accessType, categoryId, pageable);
        }

        Page<BookPublicSummaryResponse> responsePage = books.map(BookMapper::toPublicSummaryResponse);

        return PageResponse.from(responsePage);
    }

    @Transactional
    public BookPublicDetailResponse getBookDetail(Long id) {

        Book book = bookRepository.findByIdAndStatus(id, BookStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        Long currentViewCount = book.getViewCount();

        if (currentViewCount == null) {
            book.setViewCount(1L);
        } else {
            book.setViewCount(currentViewCount + 1);
        }

        Book updatedBook = bookRepository.save(book);

        return BookMapper.toPublicDetailResponse(updatedBook);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookPublicSummaryResponse> getFreeBooks(int page, int size) {

        return getBooks(null, null, BookAccessType.FREE, BookSort.NEWEST, page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookPublicSummaryResponse> getPremiumBooks(int page, int size) {

        return getBooks(null, null, BookAccessType.PREMIUM, BookSort.NEWEST, page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookPublicSummaryResponse> getNewBooks(int page, int size) {

        return getBooks(null, null, null, BookSort.NEWEST, page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookPublicSummaryResponse> getPopularBooks(int page, int size) {

        return getBooks(null, null, null, BookSort.POPULAR, page, size);
    }

    private Sort getSort(BookSort sort) {

        BookSort selectedSort = sort == null ? BookSort.NEWEST : sort;

        return switch (selectedSort) {

        case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt");

        case POPULAR -> Sort.by(Sort.Direction.DESC, "viewCount").and(Sort.by(Sort.Direction.DESC, "createdAt"));

        case TITLE_ASC -> Sort.by(Sort.Direction.ASC, "title");

        case TITLE_DESC -> Sort.by(Sort.Direction.DESC, "title");
        };
    }

    private void validateCategory(Long categoryId) {

        if (categoryId == null) {
            return;
        }

        boolean exists = categoryRepository.findByIdAndActiveTrue(categoryId).isPresent();

        if (!exists) {
            throw new BadRequestException("Category not found or inactive");
        }
    }

    private void validatePagination(int page, int size) {

        if (page < 0) {
            throw new BadRequestException("Page number must be 0 or greater");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }
    }

    private String normalizeText(String value) {

        if (value == null || value.isBlank()) {

            return null;
        }

        return value.trim().toLowerCase();
    }
}