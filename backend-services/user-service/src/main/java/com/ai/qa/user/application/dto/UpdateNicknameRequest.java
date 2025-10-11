package com.ai.qa.user.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "更新昵称请求DTO")
public class UpdateNicknameRequest {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, 
            example = "1")
    private Long userId;

    @Schema(description = "新昵称", requiredMode = Schema.RequiredMode.REQUIRED, 
            example = "新用户昵称", maxLength = 50)
    private String nickname;

    // Getter and Setter
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
