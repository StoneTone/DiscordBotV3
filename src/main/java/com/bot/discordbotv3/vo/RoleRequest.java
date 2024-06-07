package com.bot.discordbotv3.vo;

public class RoleRequest {
    private final long userId;
    private final long roleId;

    public RoleRequest(long userId, long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public long getUserId() {
        return userId;
    }

    public long getRoleId() {
        return roleId;
    }
}
