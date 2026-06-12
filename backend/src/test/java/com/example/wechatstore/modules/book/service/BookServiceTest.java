package com.example.wechatstore.modules.book.service;

import com.example.wechatstore.common.exception.BizException;
import com.example.wechatstore.modules.book.dto.BookSaveDTO;
import com.example.wechatstore.modules.book.entity.Book;
import com.example.wechatstore.modules.book.mapper.BookMapper;
import com.example.wechatstore.modules.book.vo.BookVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    @Test
    void rejectsSalePriceAboveOriginalPrice() {
        BookSaveDTO dto = new BookSaveDTO(
                "教材",
                null,
                null,
                new BigDecimal("80.00"),
                new BigDecimal("100.00"),
                null,
                10,
                0,
                1
        );

        assertThatThrownBy(() -> bookService.createBook(dto))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("优惠价不能高于原价");
        verify(bookMapper, never()).insert(any(Book.class));
    }

    @Test
    void listActiveBooksAllowsHistoricalNullOriginalPrice() {
        Book book = new Book();
        book.setId(1L);
        book.setName("历史教材");
        book.setPrice(new BigDecimal("88.00"));
        book.setOriginalPrice(null);
        book.setStock(10);
        book.setSort(0);
        book.setStatus(1);

        when(bookMapper.selectList(any())).thenReturn(List.of(book));

        List<BookVO> books = bookService.listActiveBooks();

        assertThat(books).hasSize(1);
        assertThat(books.get(0).price()).isEqualByComparingTo("88.00");
        assertThat(books.get(0).originalPrice()).isNull();
    }
}
