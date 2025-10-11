package com.ai.qa.user.api.dto;

import com.ai.qa.user.api.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

@Schema(description = "通用API响应封装类")
// @JsonInclude(JsonInclude.Include.NON_NULL) // 序列化时忽略null值的字段
public class ApiResult<T> implements Serializable {

    @Schema(description = "响应状态码", example = "200")
    private final int code;
    
    @Schema(description = "响应消息", example = "操作成功")
    private final String message;
    
    @Schema(description = "是否成功", example = "true")
    private final boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL) // 仅在data不为null时序列化
    @Schema(description = "响应数据")
    private T data;

    private ApiResult(int code, String message, boolean success, T data) {
        this.code = code;
        this.message = message;
        this.success = success;
        this.data = data;
    }

    // --- 静态工厂方法 ---

    // 成功的响应（带数据）
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), true, data);
    }

    // 成功的响应（不带数据）
    public static <T> ApiResult<T> success() {
        return success(null);
    }

    // 失败的响应
    public static <T> ApiResult<T> failure(ErrorCode errorCode) {
        return new ApiResult<>(errorCode.getCode(), errorCode.getMessage(), false, null);
    }

    // 失败的响应（可自定义覆盖错误信息）
    public static <T> ApiResult<T> failure(ErrorCode errorCode, String message) {
        return new ApiResult<>(errorCode.getCode(), message, false, null);
    }

    // --- Getters ---
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }
}
