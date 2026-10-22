package com.gxufl.coffee.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gxufl.coffee.common.Result;
import com.gxufl.coffee.entity.User;
import com.gxufl.coffee.mapper.UserMapper;
import com.gxufl.coffee.security.JwtUtil;
import com.gxufl.coffee.security.LoginUser;
import com.gxufl.coffee.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return reject(response, 401, "请先登录");
        }
        LoginUser loginUser;
        try {
            loginUser = jwtUtil.parseToken(header.substring(7));
        } catch (Exception e) {
            return reject(response, 401, "登录已过期，请重新登录");
        }
        User user = userMapper.selectById(loginUser.getId());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            return reject(response, 401, "账号不存在或已被停用");
        }
        if (request.getServletPath().startsWith("/admin") && !"ADMIN".equals(user.getRole())) {
            return reject(response, 403, "无权访问管理后台");
        }
        UserContext.set(new LoginUser(user.getId(), user.getRole()));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean reject(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Result.error(code, message));
        return false;
    }
}
