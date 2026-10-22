package com.gxufl.coffee.security;

public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new IllegalStateException("No login user in context");
        }
        return user;
    }

    public static Long userId() {
        return get().getId();
    }

    public static boolean isAdmin() {
        LoginUser user = HOLDER.get();
        return user != null && "ADMIN".equals(user.getRole());
    }

    public static void clear() {
        HOLDER.remove();
    }
}
