package com.gxufl.coffee.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gxufl.coffee.common.BizException;
import com.gxufl.coffee.dto.AuthDtos;
import com.gxufl.coffee.entity.User;
import com.gxufl.coffee.mapper.UserMapper;
import com.gxufl.coffee.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthDtos.LoginVO register(AuthDtos.Register request) {
        Long exists = userMapper.selectCount(Wrappers.<User>lambdaQuery().eq(User::getPhone, request.getPhone()));
        if (exists > 0) {
            throw new BizException("该手机号已注册，请直接登录");
        }
        User user = new User();
        user.setNickname(request.getNickname().trim());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
        return buildLogin(user);
    }

    public AuthDtos.LoginVO login(AuthDtos.Login request) {
        return doLogin(request.getPhone(), request.getPassword(), false);
    }

    public AuthDtos.LoginVO adminLogin(AuthDtos.AdminLogin request) {
        return doLogin(request.getAccount(), request.getPassword(), true);
    }

    private AuthDtos.LoginVO doLogin(String account, String password, boolean adminOnly) {
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, account).last("LIMIT 1"));
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BizException("账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException("该账号已被停用");
        }
        if (adminOnly && !"ADMIN".equals(user.getRole())) {
            throw new BizException("该账号没有管理后台权限");
        }
        return buildLogin(user);
    }

    private AuthDtos.LoginVO buildLogin(User user) {
        String token = jwtUtil.createToken(user.getId(), user.getRole());
        return new AuthDtos.LoginVO(token, toVO(user));
    }

    public AuthDtos.UserVO me(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(401, "用户不存在");
        }
        return toVO(user);
    }

    @Transactional
    public AuthDtos.UserVO updateProfile(Long userId, AuthDtos.Profile request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(401, "用户不存在");
        }
        user.setNickname(request.getNickname().trim());
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        userMapper.updateById(user);
        return toVO(user);
    }

    public static AuthDtos.UserVO toVO(User user) {
        AuthDtos.UserVO vo = new AuthDtos.UserVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
