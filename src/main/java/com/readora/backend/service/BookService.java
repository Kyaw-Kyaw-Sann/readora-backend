package com.readora.backend.service;

import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.request.CreateBookRequest;
import com.readora.backend.dto.request.UpdateBookRequest;
import com.readora.backend.dto.response.BookDetailResponse;
import com.readora.backend.dto.response.BookSummaryResponse;
import com.readora.backend.dto.response.MediaUploadResponse;
import com.readora.backend.entity.Book;
import com.readora.backend.entity.Category;
import com.readora.backend.enums.BookAccessType;
import com.readora.backend.enums.BookStatus;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.exception.ResourceNotFoundException;
import com.readora.backend.mapper.BookMapper;
import com.readora.backend.repository.BookRepository;
import com.readora.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public BookDetailResponse createBook(CreateBookRequest request, MultipartFile cover, MultipartFile pdf,
            MultipartFile audio) {

        validateIsbnForCreate(request.isbn());

        Set<Category> categories = getAndValidateCategories(request.categoryIds());

        Book book = Book.builder().title(request.title().trim()).description(normalizeText(request.description()))
                .isbn(normalizeText(request.isbn())).language(normalizeText(request.language()))
                .publicationDate(request.publicationDate()).author(request.author().trim())
                .pageCount(request.pageCount()).audioDurationSeconds(request.audioDurationSeconds())
                .accessType(request.accessType()).status(BookStatus.DRAFT).viewCount(0L).categories(categories).build();

        if (hasFile(cover)) {

            MediaUploadResponse upload = cloudinaryService.uploadCover(cover);

            deleteOnRollback(() -> cloudinaryService.deleteCover(upload.publicId()), "cover");

            book.setCoverUrl(upload.url());

            book.setCoverPublicId(upload.publicId());
        }

        if (hasFile(pdf)) {

            MediaUploadResponse upload = cloudinaryService.uploadPdf(pdf);

            deleteOnRollback(() -> cloudinaryService.deletePdf(upload.publicId()), "PDF");

            book.setPdfUrl(upload.url());

            book.setPdfPublicId(upload.publicId());
        }

        if (hasFile(audio)) {

            MediaUploadResponse upload = cloudinaryService.uploadAudio(audio);

            deleteOnRollback(() -> cloudinaryService.deleteAudio(upload.publicId()), "audio");

            book.setAudioUrl(upload.url());

            book.setAudioPublicId(upload.publicId());
        }

        Book savedBook = bookRepository.save(book);

        return BookMapper.toDetailResponse(savedBook);
    }

    @Transactional(readOnly = true)
    public BookDetailResponse getAdminBook(Long id) {

        Book book = getBookById(id);

        return BookMapper.toDetailResponse(book);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookSummaryResponse> getAdminBooks(String search, BookStatus status, BookAccessType accessType,
            Long categoryId, int page, int size) {

        validatePagination(page, size);

        String normalizedSearch = normalizeSearch(search);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Book> bookPage;

        if (normalizedSearch == null) {

            bookPage = bookRepository.findAdminBooks(status, accessType, categoryId, pageable);

        } else {

            String searchPattern = "%" + normalizedSearch + "%";

            bookPage = bookRepository.findAdminBooksBySearch(searchPattern, status, accessType, categoryId, pageable);
        }

        Page<BookSummaryResponse> responsePage = bookPage.map(BookMapper::toSummaryResponse);

        return PageResponse.from(responsePage);
    }

    @Transactional
    public BookDetailResponse updateBook(Long id, UpdateBookRequest request, MultipartFile cover, MultipartFile pdf,
            MultipartFile audio) {

        Book book = getBookById(id);

        validateIsbnForUpdate(request.isbn(), id);

        boolean removeCover = Boolean.TRUE.equals(request.removeCover());
        boolean removePdf = Boolean.TRUE.equals(request.removePdf());
        boolean removeAudio = Boolean.TRUE.equals(request.removeAudio());

        validateMediaActions(removeCover, removePdf, removeAudio, cover, pdf, audio);

        Set<Category> categories = getAndValidateCategories(request.categoryIds());

        validatePublishedUpdate(book, categories, cover, pdf, audio, removeCover, removePdf, removeAudio);

        book.setTitle(request.title().trim());

        book.setDescription(normalizeText(request.description()));

        book.setIsbn(normalizeText(request.isbn()));

        book.setLanguage(normalizeText(request.language()));

        book.setPublicationDate(request.publicationDate());

        book.setAuthor(request.author().trim());

        book.setPageCount(request.pageCount());

        book.setAudioDurationSeconds(request.audioDurationSeconds());

        book.setAccessType(request.accessType());

        book.setCategories(categories);

        updateCover(book, cover, removeCover);

        updatePdf(book, pdf, removePdf);

        updateAudio(book, audio, removeAudio);

        Book updatedBook = bookRepository.save(book);

        return BookMapper.toDetailResponse(updatedBook);
    }

    @Transactional
    public BookDetailResponse publishBook(Long id) {

        Book book = getBookById(id);

        if (book.getStatus() == BookStatus.PUBLISHED) {

            throw new BadRequestException("Book is already published");
        }

        if (book.getStatus() == BookStatus.ARCHIVED) {

            throw new BadRequestException("Archived book cannot be published");
        }

        validateBookForPublishing(book);

        book.setStatus(BookStatus.PUBLISHED);

        Book publishedBook = bookRepository.save(book);

        return BookMapper.toDetailResponse(publishedBook);
    }

    @Transactional
    public BookDetailResponse archiveBook(Long id) {

        Book book = getBookById(id);

        if (book.getStatus() == BookStatus.ARCHIVED) {

            throw new BadRequestException("Book is already archived");
        }

        book.setStatus(BookStatus.ARCHIVED);

        Book archivedBook = bookRepository.save(book);

        return BookMapper.toDetailResponse(archivedBook);
    }

    private void validateBookForPublishing(Book book) {

        boolean hasActiveCategory = book.getCategories().stream().anyMatch(Category::isActive);

        if (!hasActiveCategory) {

            throw new BadRequestException("Book must have at least one active category before publishing");
        }

        if (book.getCoverUrl() == null || book.getCoverUrl().isBlank()) {

            throw new BadRequestException("Book must have a cover before publishing");
        }

        boolean hasPdf = book.getPdfUrl() != null && !book.getPdfUrl().isBlank();

        boolean hasAudio = book.getAudioUrl() != null && !book.getAudioUrl().isBlank();

        if (!hasPdf && !hasAudio) {

            throw new BadRequestException("Book must have a PDF or audio before publishing");
        }
    }

    private void validatePublishedUpdate(Book book, Set<Category> categories, MultipartFile cover,
            MultipartFile pdf, MultipartFile audio, boolean removeCover, boolean removePdf, boolean removeAudio) {

        if (book.getStatus() != BookStatus.PUBLISHED) {
            return;
        }

        boolean hasActiveCategory = categories.stream().anyMatch(Category::isActive);

        if (!hasActiveCategory) {
            throw new BadRequestException("Published book must have at least one active category");
        }

        boolean willHaveCover = hasFile(cover)
                || (!removeCover && book.getCoverUrl() != null && !book.getCoverUrl().isBlank());

        if (!willHaveCover) {
            throw new BadRequestException("Published book must have a cover");
        }

        boolean willHavePdf = hasFile(pdf)
                || (!removePdf && book.getPdfUrl() != null && !book.getPdfUrl().isBlank());
        boolean willHaveAudio = hasFile(audio)
                || (!removeAudio && book.getAudioUrl() != null && !book.getAudioUrl().isBlank());

        if (!willHavePdf && !willHaveAudio) {
            throw new BadRequestException("Published book must have a PDF or audio");
        }
    }

    private void updateCover(Book book, MultipartFile cover, boolean removeCover) {

        if (removeCover) {

            if (book.getCoverPublicId() != null) {

                String oldPublicId = book.getCoverPublicId();
                deleteAfterCommit(() -> cloudinaryService.deleteCover(oldPublicId), "cover");
            }

            book.setCoverUrl(null);
            book.setCoverPublicId(null);

            return;
        }

        if (hasFile(cover)) {

            String oldPublicId = book.getCoverPublicId();
            MediaUploadResponse upload = cloudinaryService.uploadCover(cover);

            deleteOnRollback(() -> cloudinaryService.deleteCover(upload.publicId()), "cover");

            if (oldPublicId != null) {
                deleteAfterCommit(() -> cloudinaryService.deleteCover(oldPublicId), "cover");
            }

            book.setCoverUrl(upload.url());

            book.setCoverPublicId(upload.publicId());
        }
    }

    private void updatePdf(Book book, MultipartFile pdf, boolean removePdf) {

        if (removePdf) {

            if (book.getPdfPublicId() != null) {

                String oldPublicId = book.getPdfPublicId();
                deleteAfterCommit(() -> cloudinaryService.deletePdf(oldPublicId), "PDF");
            }

            book.setPdfUrl(null);
            book.setPdfPublicId(null);

            return;
        }

        if (hasFile(pdf)) {

            String oldPublicId = book.getPdfPublicId();
            MediaUploadResponse upload = cloudinaryService.uploadPdf(pdf);

            deleteOnRollback(() -> cloudinaryService.deletePdf(upload.publicId()), "PDF");

            if (oldPublicId != null) {
                deleteAfterCommit(() -> cloudinaryService.deletePdf(oldPublicId), "PDF");
            }

            book.setPdfUrl(upload.url());

            book.setPdfPublicId(upload.publicId());
        }
    }

    private void updateAudio(Book book, MultipartFile audio, boolean removeAudio) {

        if (removeAudio) {

            if (book.getAudioPublicId() != null) {

                String oldPublicId = book.getAudioPublicId();
                deleteAfterCommit(() -> cloudinaryService.deleteAudio(oldPublicId), "audio");
            }

            book.setAudioUrl(null);
            book.setAudioPublicId(null);

            return;
        }

        if (hasFile(audio)) {

            String oldPublicId = book.getAudioPublicId();
            MediaUploadResponse upload = cloudinaryService.uploadAudio(audio);

            deleteOnRollback(() -> cloudinaryService.deleteAudio(upload.publicId()), "audio");

            if (oldPublicId != null) {
                deleteAfterCommit(() -> cloudinaryService.deleteAudio(oldPublicId), "audio");
            }

            book.setAudioUrl(upload.url());

            book.setAudioPublicId(upload.publicId());
        }
    }

    private Set<Category> getAndValidateCategories(List<Long> categoryIds) {

        Set<Long> uniqueIds = new HashSet<>(categoryIds);

        if (uniqueIds.size() != categoryIds.size()) {

            throw new BadRequestException("Duplicate category IDs are not allowed");
        }

        List<Category> categories = categoryRepository.findAllById(categoryIds);

        if (categories.size() != categoryIds.size()) {

            throw new BadRequestException("One or more categories not found");
        }

        boolean hasInactiveCategory = categories.stream().anyMatch(category -> !category.isActive());

        if (hasInactiveCategory) {

            throw new BadRequestException("Inactive categories cannot be assigned to a book");
        }

        return new HashSet<>(categories);
    }

    private void validateIsbnForCreate(String isbn) {

        String normalizedIsbn = normalizeText(isbn);

        if (normalizedIsbn != null && bookRepository.existsByIsbn(normalizedIsbn)) {

            throw new BadRequestException("ISBN already exists");
        }
    }

    private void validateIsbnForUpdate(String isbn, Long bookId) {

        String normalizedIsbn = normalizeText(isbn);

        if (normalizedIsbn != null && bookRepository.existsByIsbnAndIdNot(normalizedIsbn, bookId)) {

            throw new BadRequestException("ISBN already exists");
        }
    }

    private void validateMediaActions(boolean removeCover, boolean removePdf, boolean removeAudio,
            MultipartFile cover, MultipartFile pdf, MultipartFile audio) {

        if (removeCover && hasFile(cover)) {

            throw new BadRequestException("Cannot upload and remove cover at the same time");
        }

        if (removePdf && hasFile(pdf)) {

            throw new BadRequestException("Cannot upload and remove PDF at the same time");
        }

        if (removeAudio && hasFile(audio)) {

            throw new BadRequestException("Cannot upload and remove audio at the same time");
        }
    }

    private void deleteOnRollback(Runnable deleteAction, String mediaType) {

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCompletion(int status) {

                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    runCleanup(deleteAction, mediaType, "rollback");
                }
            }
        });
    }

    private void deleteAfterCommit(Runnable deleteAction, String mediaType) {

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCompletion(int status) {

                if (status == TransactionSynchronization.STATUS_COMMITTED) {
                    runCleanup(deleteAction, mediaType, "commit");
                }
            }
        });
    }

    private void runCleanup(Runnable deleteAction, String mediaType, String transactionOutcome) {

        try {
            deleteAction.run();
        } catch (RuntimeException ex) {
            log.error("Failed to clean up {} media after transaction {}", mediaType, transactionOutcome, ex);
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

    private Book getBookById(Long id) {

        return bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book not found"));
    }

    private boolean hasFile(MultipartFile file) {

        return file != null && !file.isEmpty();
    }

    private String normalizeText(String value) {

        if (value == null || value.isBlank()) {

            return null;
        }

        return value.trim();
    }

    private String normalizeSearch(String value) {

        if (value == null || value.isBlank()) {

            return null;
        }

        return value.trim().toLowerCase();
    }
}
