package com.readora.backend.entity;

import com.readora.backend.enums.BookAccessType;
import com.readora.backend.enums.BookStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(unique = true, length = 50)
    private String isbn;

    @Column(length = 50)
    private String language;

    private LocalDate publicationDate;

    @Column(nullable = false, length = 150)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String coverUrl;

    @Column(length = 255)
    private String coverPublicId;

    @Column(columnDefinition = "TEXT")
    private String pdfUrl;

    @Column(length = 255)
    private String pdfPublicId;

    @Column(columnDefinition = "TEXT")
    private String audioUrl;

    @Column(length = 255)
    private String audioPublicId;

    private Integer pageCount;

    private Integer audioDurationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookAccessType accessType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookStatus status;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "book_categories", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @Override
    protected void onCreate() {
        super.onCreate();
        if (viewCount == null) {
            viewCount = 0L;
        }
    }
}