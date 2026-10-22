package com.gxufl.coffee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

public final class AuthDtos {

    private AuthDtos() {
    }

    @Data
    public static class Register {
        @NotBlank(message = "请填写昵称")
        @Size(max = 20, message = "昵称最多20个字符")
        private String nickname;

        @NotBlank(message = "请填写手机号")
        @Pattern(regexp = "^1\\d{10}$", message = "请输入11位手机号")
        private String phone;

        @NotBlank(message = "请填写密码")
        @Size(min = 6, max = 32, message = "密码需为6-32位")
        private String password;
    }

    @Data
    public static class Login {
        @NotBlank(message = "请填写手机号")
        private String phone;

        @NotBlank(message = "请填写密码")
        private String password;
    }

    @Data
    public static class AdminLogin {
        @NotBlank(message = "请填写管理员账号")
        private String account;

        @NotBlank(message = "请填写密码")
        private String password;
    }

    @Data
    public static class Profile {
        @NotBlank(message = "请填写昵称")
        @Size(max = 20, message = "昵称最多20个字符")
        private String nickname;
        private String avatar;
    }

    @Data
    public static class UserVO {
        private Long id;
        private String nickname;
        private String phone;
        private String avatar;
        private String role;
        private Integer status;
        private LocalDateTime createdAt;
    }

    @Data
    public static class LoginVO {
        private String token;
        private UserVO user;

        public LoginVO(String token, UserVO user) {
            this.token = token;
            this.user = user;
        }
    }
}
