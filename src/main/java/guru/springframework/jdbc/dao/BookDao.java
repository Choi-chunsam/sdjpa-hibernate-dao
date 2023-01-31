package guru.springframework.jdbc.dao;

import guru.springframework.jdbc.domain.Book;

import java.util.List;

public interface BookDao {

    List<Book> findAll();

    Book findByISBN(String isbn);

    Book getById(Long id);

    Book findByTitle(String title);

    Book saveNewBook(Book book);

    Book updateBook(Book book);

    void deleteById(Long id);

    Book findByTitleCriteria(String title);

    Book findByTitleNative(String title);
}
