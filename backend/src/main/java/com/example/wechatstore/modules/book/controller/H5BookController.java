package com.example.wechatstore.modules.book.controller;

import com.example.wechatstore.common.result.Result;
import com.example.wechatstore.modules.book.service.BookService;
import com.example.wechatstore.modules.book.vo.BookVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/h5/books")
/**
 * H5 书籍控制器。
 * <p>
 * 负责返回前台可见的上架书籍列表。
 * </p>
 */
public class H5BookController {

    private final BookService bookService;

    public H5BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 查询上架书籍列表。
     *
     * @return 书籍列表
     */
    @GetMapping
    public Result<List<BookVO>> listBooks() {
        return Result.ok(bookService.listActiveBooks());
    }
}
