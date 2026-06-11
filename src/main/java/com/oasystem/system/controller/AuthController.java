package com.oasystem.system.controller;

import com.oasystem.common.Result;
import com.oasystem.log.annotation.Log;
import com.oasystem.system.dto.LoginDTO;
import com.oasystem.system.service.UserService;
import com.oasystem.system.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * <p>
 * 处理登录、登出、获取当前用户信息等认证相关请求。
 */
@Tag(name = "认证管理", description = "登录、登出、用户信息")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Log(module = "认证管理", value = "用户登录")
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @Parameter(description = "登录凭证（用户名+密码）", required = true) @Valid @RequestBody LoginDTO loginDTO) {
        Map<String, Object> result = userService.login(loginDTO);
        return Result.ok("登录成功", result);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public Result<UserVO> getUserInfo() {
        UserVO userVO = userService.getCurrentUserInfo();
        return Result.ok(userVO);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // TODO: 将Token加入黑名单或清除Redis中的Token
        return Result.ok();
    }
}
