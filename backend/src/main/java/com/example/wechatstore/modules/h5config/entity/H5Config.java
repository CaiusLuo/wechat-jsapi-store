package com.example.wechatstore.modules.h5config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("h5_config")
public class H5Config {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String siteTitle;
    private String siteSubtitle;
    private String serviceWechat;
    private String servicePhone;
    private String workTime;
    private String noticeText;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
