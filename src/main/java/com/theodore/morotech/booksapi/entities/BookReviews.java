package com.theodore.morotech.booksapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Table(name = "book_reviews",
        indexes = {
                @Index(name = "idx_book_reviews_book_id", columnList = "book_id"),
                @Index(name = "idx_book_reviews_created_at", columnList = "created_at")
        })
@Entity
public class BookReviews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "book_id", updatable = false, nullable = false)
    Long bookId;

    @Min(1)
    @Max(5)
    @Column(name = "rating", nullable = false)
    int rating;

    @NotBlank
    @Column(name = "review", nullable = false)
    String review;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    Instant createdAt;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
