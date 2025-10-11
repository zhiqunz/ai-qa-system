package com.ai.qa.user.api.controller;

import com.ai.qa.user.api.dto.ApiResult;
import com.ai.qa.user.api.dto.AuthRequest;
import com.ai.qa.user.api.dto.AuthResponse;
import com.ai.qa.user.api.dto.UserResponse;
import com.ai.qa.user.application.dto.UpdateNicknameRequest;
import com.ai.qa.user.application.mapper.UserMapper;
import com.ai.qa.user.application.service.AuthApplicationService;
import com.ai.qa.user.application.service.UserApplicationService;
import com.ai.qa.user.api.exception.BusinessException;
import com.ai.qa.user.api.exception.ErrorCode;
import com.ai.qa.user.domain.model.User;
import com.ai.qa.user.infrastructure.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/***
 * 为什么user-service必须也要自己做安全限制？
 * 1。零信任网络 (Zero Trust Network)：在微服务架构中，你必须假设内部网络是不安全的。不能因为一个请求来自API
 * Gateway就完全信任它。万一有其他内部服务被攻破，它可能会伪造请求直接调用user-service，绕过Gateway。如果user-service没有自己的安全防线，它就会被完全暴露。
 * 2。职责分离 (Separation of
 * Concerns)：Gateway的核心职责是路由、限流、熔断和边缘认证。而user-service的核心职责是处理用户相关的业务逻辑，业务逻辑与谁能执行它是密不可分的。授权逻辑是业务逻辑的一部分，必须放在离业务最近的地方。
 * 3。细粒度授权 (Fine-Grained
 * Authorization)：Gateway通常只做粗粒度的授权，比如“USER角色的用户可以访问/api/users/**这个路径”。但它无法知道更精细的业务规则，例如：
 * GET /api/users/{userId}: 用户123是否有权查看用户456的资料？
 * PUT /api/users/{userId}: 只有用户自己或者管理员才能修改用户信息。
 * 这些判断必须由user-service结合自身的业务逻辑和数据来完成。
 */
@Tag(name = "用户管理", description = "用户服务相关接口")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserApplicationService userApplicationService;
    private final AuthApplicationService authApplicationService;

    public UserController(UserApplicationService userApplicationService, AuthApplicationService authApplicationService) {
        this.userApplicationService = userApplicationService;
        this.authApplicationService = authApplicationService;
    }

    /**
     * 更新用户昵称的API端点
     *
     * @param userId  从URL路径中获取的用户ID
     * @param request 包含新昵称的请求体
     * @return 返回更新后的用户信息和HTTP状态码200 (OK)
     */
    @Operation(summary = "更新用户昵称", description = "根据用户ID更新用户的昵称信息，只能修改自己的昵称", security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "昵称更新成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "未授权，只能修改自己的昵称"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PutMapping("/{userId}/nickname")
    public ApiResult<UserResponse> updateNickname(
            @Parameter(description = "用户ID", required = true, example = "1") @PathVariable Long userId,
            @Parameter(description = "更新昵称请求体", required = true) @RequestBody UpdateNicknameRequest request) {
        
        // 第一步：校验路径参数与请求体中的userId一致
        if (!userId.equals(request.getUserId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "路径参数userId与请求体中的userId不一致");
        }
        
        // 第二步：权限校验 - 只能修改自己的昵称
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未认证");
        }
        
        // 获取当前登录用户的信息
        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();
        
        // 校验只能修改自己的信息
        if (!currentUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "只能修改自己的昵称");
        }

        // 控制器只负责调用应用层，不处理业务逻辑
        User updatedUser = userApplicationService.updateNickname(userId, request.getNickname());
        
        // 使用MapStruct将领域实体转换为DTO，避免直接暴露领域模型
        UserResponse userResponse = UserMapper.INSTANCE.toResponse(updatedUser);
        return ApiResult.success(userResponse);
    }

    @Operation(summary = "用户登录", description = "用户登录认证接口，返回统一封装的认证响应")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功，返回统一封装的认证信息和令牌"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "认证失败")
    })
    @PostMapping("/login")
    public ApiResult<AuthResponse> login(@Parameter(description = "登录请求体", required = true) @RequestBody AuthRequest request) {
        AuthResponse authResponse = authApplicationService.login(request);
        return ApiResult.success(authResponse);
    }

    @Operation(summary = "用户注册", description = "新用户注册接口，返回统一封装的认证响应")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功，返回统一封装的认证信息和令牌"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "409", description = "用户名已存在")
    })
    @PostMapping("/register")
    public ApiResult<AuthResponse> register(@Parameter(description = "注册请求体", required = true) @RequestBody AuthRequest request) {
        AuthResponse authResponse = authApplicationService.register(request);
        return ApiResult.success(authResponse);
    }

    @Operation(summary = "根据ID查询用户", description = "根据用户ID查询用户信息，返回统一封装的响应", security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功，返回统一封装的用户信息"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @GetMapping("/{userId}")
    public ApiResult<String> getUserById(
            @Parameter(description = "用户ID", required = true, example = "1") @PathVariable("userId") Long userId) {
        System.out.println("测试userid");
        // TODO: 实际业务逻辑 - 根据用户ID查询用户信息
        String userInfo = "userid:" + userId;
        return ApiResult.success(userInfo);
    }
}
