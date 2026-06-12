package com.example.wechatstore.modules.book.controller;

import com.example.wechatstore.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wechatstore.modules.book.dto.AdminBookQueryDTO;
import com.example.wechatstore.modules.book.dto.BookSaveDTO;
import com.example.wechatstore.modules.book.dto.BookStatusDTO;
import com.example.wechatstore.modules.book.service.BookService;
import com.example.wechatstore.modules.book.vo.BookVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/books")
public class AdminBookController {

    private final BookService bookService;

    public AdminBookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Result<Page<BookVO>> listBooks(AdminBookQueryDTO query) {
        return Result.ok(bookService.listAdminBooks(query));
    }

    @PostMapping
    public Result<BookVO> createBook(@Valid @RequestBody BookSaveDTO dto) {
        return Result.ok(bookService.createBook(dto));
    }

    @PutMapping("/{id}")
    public Result<BookVO> updateBook(@PathVariable Long id, @Valid @RequestBody BookSaveDTO dto) {
        return Result.ok(bookService.updateBook(id, dto));
    }

    @PatchMapping("/{id}/status")
    public Result<BookVO> updateStatus(@PathVariable Long id, @Valid @RequestBody BookStatusDTO dto) {
        return Result.ok(bookService.updateStatus(id, dto.status()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.ok();
    }
}
