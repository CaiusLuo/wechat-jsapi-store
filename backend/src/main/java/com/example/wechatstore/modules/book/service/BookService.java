package com.example.wechatstore.modules.book.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wechatstore.common.exception.BizException;
import com.example.wechatstore.modules.book.dto.AdminBookQueryDTO;
import com.example.wechatstore.modules.book.dto.BookSaveDTO;
import com.example.wechatstore.modules.book.entity.Book;
import com.example.wechatstore.modules.book.mapper.BookMapper;
import com.example.wechatstore.modules.book.vo.BookVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private final BookMapper bookMapper;

    public BookService(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    public List<BookVO> listActiveBooks() {
        return bookMapper.selectList(baseQuery()
                        .eq(Book::getStatus, 1)
                        .orderByDesc(Book::getSort)
                        .orderByAsc(Book::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    public Page<BookVO> listAdminBooks(AdminBookQueryDTO queryDTO) {
        AdminBookQueryDTO query = queryDTO == null ? new AdminBookQueryDTO() : queryDTO;
        long current = normalizePage(query.getPage());
        long size = normalizeSize(query.getSize());
        Long total = bookMapper.selectCount(buildAdminQuery(query, false));
        List<Book> books = total == 0
                ? List.of()
                : bookMapper.selectList(buildAdminQuery(query, true)
                .last("limit " + size + " offset " + ((current - 1) * size)));

        Page<BookVO> result = new Page<>(current, size, total);
        result.setRecords(books.stream().map(this::toVO).toList());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public BookVO createBook(BookSaveDTO dto) {
        Book book = new Book();
        book.setDeleted(0);
        applySaveDTO(book, dto);
        bookMapper.insert(book);
        return toVO(book);
    }

    @Transactional(rollbackFor = Exception.class)
    public BookVO updateBook(Long id, BookSaveDTO dto) {
        Book book = requireBook(id);
        applySaveDTO(book, dto);
        bookMapper.updateById(book);
        return toVO(book);
    }

    @Transactional(rollbackFor = Exception.class)
    public BookVO updateStatus(Long id, Integer status) {
        Book book = requireBook(id);
        book.setStatus(status);
        bookMapper.updateById(book);
        return toVO(book);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBook(Long id) {
        Book book = requireBook(id);
        book.setDeleted(1);
        bookMapper.updateById(book);
    }

    private LambdaQueryWrapper<Book> baseQuery() {
        return new LambdaQueryWrapper<Book>().eq(Book::getDeleted, 0);
    }

    private LambdaQueryWrapper<Book> buildAdminQuery(AdminBookQueryDTO query, boolean withOrder) {
        LambdaQueryWrapper<Book> wrapper = baseQuery();
        if (StringUtils.hasText(query.getName())) {
            String keyword = query.getName().trim();
            wrapper.and(item -> item.like(Book::getName, keyword).or().like(Book::getSubtitle, keyword));
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Book::getStatus, parseStatus(query.getStatus()));
        }
        if (withOrder) {
            wrapper.orderByDesc(Book::getSort).orderByAsc(Book::getId);
        }
        return wrapper;
    }

    private Book requireBook(Long id) {
        Book book = bookMapper.selectOne(baseQuery()
                .eq(Book::getId, id)
                .last("limit 1"));
        if (book == null) {
            throw new BizException(404, "book not found");
        }
        return book;
    }

    private void applySaveDTO(Book book, BookSaveDTO dto) {
        validatePrice(dto.originalPrice(), dto.price());
        book.setName(dto.name());
        book.setSubtitle(dto.subtitle());
        book.setCoverUrl(dto.coverUrl());
        book.setOriginalPrice(dto.originalPrice());
        book.setPrice(dto.price());
        book.setIntro(dto.intro());
        book.setStock(dto.stock() == null ? 0 : dto.stock());
        book.setSort(dto.sort() == null ? 0 : dto.sort());
        book.setStatus(dto.status() == null ? 1 : dto.status());
    }

    private BookVO toVO(Book book) {
        return new BookVO(
                book.getId(),
                book.getName(),
                book.getSubtitle(),
                book.getCoverUrl(),
                book.getOriginalPrice(),
                book.getPrice(),
                book.getIntro(),
                book.getStock(),
                book.getSort(),
                book.getStatus()
        );
    }

    private void validatePrice(BigDecimal originalPrice, BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new BizException("优惠价必须大于 0");
        }
        if (originalPrice == null || originalPrice.signum() <= 0) {
            throw new BizException("原价必须大于 0");
        }
        if (originalPrice.compareTo(price) < 0) {
            throw new BizException("优惠价不能高于原价");
        }
    }

    private long normalizePage(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    private long normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private int parseStatus(String status) {
        try {
            int value = Integer.parseInt(status.trim());
            if (value == 0 || value == 1) {
                return value;
            }
        } catch (NumberFormatException ignored) {
            // Fall through to the business error below.
        }
        throw new BizException("invalid book status");
    }
}
