package com.ai.qa.user.application.service;

import com.ai.qa.user.api.dto.AuthRequest;
import com.ai.qa.user.api.dto.AuthResponse;
import com.ai.qa.user.api.exception.BusinessException;
import com.ai.qa.user.api.exception.ErrorCode;
import com.ai.qa.user.domain.model.User;
import com.ai.qa.user.domain.repositories.UserRepository;
import com.ai.qa.user.infrastructure.security.JwtTokenProvider;
import com.ai.qa.user.infrastructure.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthApplicationService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            String token = tokenProvider.generateToken(authentication);
            
            // 获取用户信息用于响应
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
            
            return new AuthResponse(token, user.getId(), user.getUsername(), user.getNickname());
        } catch (Exception ex) {
            log.error("登录失败: {}", ex.getMessage());
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名或密码错误");
        }
    }

    public AuthResponse register(AuthRequest request) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名已存在");
        }

        // 创建新用户
        User newUser = new User(request.getUsername(), passwordEncoder.encode(request.getPassword()));
        newUser.setNickname(request.getUsername()); // 默认昵称为用户名

        User savedUser = userRepository.save(newUser);
        log.info("用户注册成功: {}", savedUser.getUsername());

        // 生成token并返回完整信息 - 创建正确的Authentication对象
        UserPrincipal userPrincipal = UserPrincipal.create(savedUser);
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        String token = tokenProvider.generateToken(authentication);
        
        return new AuthResponse(token, savedUser.getId(), savedUser.getUsername(), savedUser.getNickname());
    }
}