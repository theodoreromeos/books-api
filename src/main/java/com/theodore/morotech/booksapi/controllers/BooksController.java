package com.theodore.morotech.booksapi.controllers;

import com.theodore.morotech.booksapi.models.responses.BookSearchResponse;
import com.theodore.morotech.booksapi.services.BookService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BooksController {

    private final BookService service;

    public BooksController(BookService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public BookSearchResponse search(
            @RequestParam @NotBlank(message = "Title must not be empty")
            String title,
            @RequestParam (defaultValue = "0")
            @Min(value = 0, message = "Page must be greater than or equal to 0")
            int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 5, message = "Size must be at least 5")
            @Max(value = 100, message = "Size cannot exceed 100")
            int size) {
        return service.searchByTitle(title, page, size);
    }

}
