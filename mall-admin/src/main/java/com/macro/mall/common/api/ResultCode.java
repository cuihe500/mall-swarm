package com.macro.mall.common.api;

/**
 * API返回码封装
 */
public enum ResultCode implements IErrorCode {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(404, "参数检验失败"),
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    FORBIDDEN(403, "没有相关权限"),
    DUPLICATE_VALUE(510, "数据已存在"),
    
    // 通用业务错误码 (600-699)
    PARAM_ERROR(600, "参数错误"),
    DATA_NOT_FOUND(601, "数据不存在"),
    DATA_INVALID(602, "数据无效"),
    OPERATION_NOT_ALLOWED(603, "操作不允许"),
    STATUS_ERROR(604, "状态错误"),
    
    // 用户相关错误码 (700-799)
    USER_NOT_FOUND(700, "用户不存在"),
    USER_PASSWORD_ERROR(701, "用户密码错误"),
    USER_DISABLED(702, "用户已被禁用"),
    USER_ALREADY_EXISTS(703, "用户已存在"),
    
    // 权限相关错误码 (800-899)
    PERMISSION_DENIED(800, "权限不足"),
    ROLE_NOT_FOUND(801, "角色不存在"),
    ROLE_ALREADY_EXISTS(802, "角色已存在"),
    
    // 业务操作相关错误码 (900-999)
    RESOURCE_NOT_FOUND(900, "资源不存在"),
    RESOURCE_ALREADY_EXISTS(901, "资源已存在"),
    OPERATION_FAILED(902, "操作失败"),
    STATUS_UPDATE_FAILED(903, "状态更新失败"),
    
    // 区域管理相关错误码 (1000-1099)
    REGION_NOT_FOUND(1000, "区域不存在"),
    REGION_PARENT_NOT_FOUND(1001, "父级区域不存在"),
    REGION_LEVEL_INVALID(1002, "区域级别无效"),
    REGION_NAME_EMPTY(1003, "区域名称不能为空"),
    REGION_NAME_DUPLICATE(1004, "区域名称已存在"),
    REGION_PARENT_LEVEL_ERROR(1005, "父级区域级别必须小于当前级别"),
    REGION_CODE_INVALID(1006, "区域编码格式无效");

    private long code;
    private String message;

    private ResultCode(long code, String message) {
        this.code = code;
        this.message = message;
    }

    public long getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
} 