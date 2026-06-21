package com.theodore.morotech.booksapi.models.responses;

import java.io.Serializable;
import java.util.List;

public record BookResponse(long id,
                           String title,
                           List<AuthorResponse> authors,
                           List<String> languages,
                           Integer downloadCount) implements Serializable {
}
