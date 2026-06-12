package com.example.wechatstore.modules.book.dto;

import lombok.Data;

@Data
public class AdminBookQueryDTO {

    private Integer page;
    private Integer size;
    private String name;
    private String status;
}
