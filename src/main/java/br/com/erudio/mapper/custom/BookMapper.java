package br.com.erudio.mapper.custom;

import br.com.erudio.data.dto.v1.BookDTO;
import br.com.erudio.model.Book;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class BookMapper {

    public BookDTO convertEntityToDTO(Book book){
        BookDTO dto = new BookDTO();

        dto.setId(book.getId());
        dto.setAuthor(book.getAuthor());
        dto.setLaunch_date(book.getLaunch_date());
        dto.setPrice(book.getPrice());
        dto.setTitle(book.getTitle());

        return dto;
    }

    public Book convertDTOToEntity(BookDTO book) {
        Book entity = new Book();

        entity.setId(book.getId());
        entity.setAuthor(book.getAuthor());
        entity.setLaunch_date(book.getLaunch_date());
        entity.setPrice(book.getPrice());
        entity.setTitle(book.getTitle());

        return entity;
    }
}
