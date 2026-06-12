package com.example.wechatstore.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wx_user")
public class WxUser {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private String nickname;
    private String avatar;
    private String receiverName;
    private String phone;
    private String school;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
