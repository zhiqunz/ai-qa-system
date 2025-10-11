package com.ai.qa.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "认证请求DTO")
@Data
public class AuthRequest {
    
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "john_doe")
    private String username;
    
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "password123")
    private String password;
}