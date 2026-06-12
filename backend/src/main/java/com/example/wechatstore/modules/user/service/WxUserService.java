package com.example.wechatstore.modules.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.wechatstore.common.exception.UnauthorizedException;
import com.example.wechatstore.modules.user.dto.UpdateUserProfileDTO;
import com.example.wechatstore.modules.user.entity.WxUser;
import com.example.wechatstore.modules.user.mapper.WxUserMapper;
import com.example.wechatstore.modules.user.vo.UserProfileVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
public class WxUserService {

    private final WxUserMapper wxUserMapper;

    public WxUserService(WxUserMapper wxUserMapper) {
        this.wxUserMapper = wxUserMapper;
    }

    public WxUser findOrCreateByOpenid(String openid) {
        return findOrCreateByOpenid(openid, null, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public WxUser findOrCreateByOpenid(String openid, String nickname, String avatar) {
        WxUser existing = wxUserMapper.selectOne(new LambdaQueryWrapper<WxUser>()
                .eq(WxUser::getOpenid, openid)
                .last("limit 1"));
        if (existing != null) {
            boolean changed = false;
            if (StringUtils.hasText(nickname) && !Objects.equals(nickname, existing.getNickname())) {
                existing.setNickname(nickname);
                changed = true;
            }
            if (StringUtils.hasText(avatar) && !Objects.equals(avatar, existing.getAvatar())) {
                existing.setAvatar(avatar);
                changed = true;
            }
            if (changed) {
                wxUserMapper.updateById(existing);
            }
            return existing;
        }

        WxUser user = new WxUser();
        user.setOpenid(openid);
        if (StringUtils.hasText(nickname)) {
            user.setNickname(nickname);
        }
        if (StringUtils.hasText(avatar)) {
            user.setAvatar(avatar);
        }
        wxUserMapper.insert(user);
        return user;
    }

    public UserProfileVO getProfile(Long userId, String openid) {
        return toProfileVO(requireUser(userId, openid));
    }

    @Transactional(rollbackFor = Exception.class)
    public UserProfileVO updateProfile(Long userId, String openid, UpdateUserProfileDTO dto) {
        WxUser user = requireUser(userId, openid);
        if (dto.nickname() != null) {
            user.setNickname(dto.nickname());
        }
        if (dto.avatar() != null) {
            user.setAvatar(dto.avatar());
        }
        user.setReceiverName(dto.receiverName());
        user.setPhone(dto.phone());
        user.setSchool(dto.school());
        user.setProvince(dto.province());
        user.setCity(dto.city());
        user.setDistrict(dto.district());
        user.setDetailAddress(dto.detailAddress());
        wxUserMapper.updateById(user);
        return toProfileVO(user);
    }

    private WxUser requireUser(Long userId, String openid) {
        WxUser user = wxUserMapper.selectById(userId);
        if (user == null || !Objects.equals(user.getOpenid(), openid)) {
            throw new UnauthorizedException("h5 login required");
        }
        return user;
    }

    private UserProfileVO toProfileVO(WxUser user) {
        return new UserProfileVO(
                user.getOpenid(),
                user.getNickname(),
                user.getAvatar(),
                user.getReceiverName(),
                user.getPhone(),
                user.getSchool(),
                user.getProvince(),
                user.getCity(),
                user.getDistrict(),
                user.getDetailAddress(),
                isProfileCompleted(user)
        );
    }

    private boolean isProfileCompleted(WxUser user) {
        return StringUtils.hasText(user.getReceiverName())
                && StringUtils.hasText(user.getPhone())
                && StringUtils.hasText(user.getSchool())
                && StringUtils.hasText(user.getProvince())
                && StringUtils.hasText(user.getCity())
                && StringUtils.hasText(user.getDistrict())
                && StringUtils.hasText(user.getDetailAddress());
    }
}
