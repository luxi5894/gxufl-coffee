package com.gxufl.coffee.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxufl.coffee.common.BizException;
import com.gxufl.coffee.common.PageResult;
import com.gxufl.coffee.common.PageUtil;
import com.gxufl.coffee.dto.AuthDtos;
import com.gxufl.coffee.entity.User;
import com.gxufl.coffee.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public PageResult<AuthDtos.UserVO> list(String keyword, Integer page, Integer pageSize) {
        Page<User> result = userMapper.selectPage(PageUtil.of(page, pageSize),
                Wrappers.<User>lambdaQuery()
                        .eq(User::getRole, "USER")
                        .and(keyword != null && !keyword.isBlank(),
                                w -> w.like(User::getNickname, keyword).or().like(User::getPhone, keyword))
                        .orderByDesc(User::getId));
        return PageResult.of(result, result.getRecords().stream().map(AuthService::toVO).toList());
    }

    public void updateStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null || "ADMIN".equals(user.getRole())) {
            throw new BizException("用户不存在");
        }
        user.setStatus(status == 1 ? 1 : 0);
        userMapper.updateById(user);
    }
}
