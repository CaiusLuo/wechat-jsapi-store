package com.example.wechatstore.modules.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("book")
public class Book {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String subtitle;
    private String coverUrl;
    private BigDecimal originalPrice;
    private BigDecimal price;
    private Integer stock;
    private String intro;
    private Integer sort;
    private Integer status;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
