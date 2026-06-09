package com.oasystem.system.controller;

import com.oasystem.common.PageResult;
import com.oasystem.common.Result;
import com.oasystem.system.dto.UserQueryDTO;
import com.oasystem.system.entity.User;
import com.oasystem.system.service.UserService;
import com.oasystem.system.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理", description = "用户CURD、密码重置")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    public Result<PageResult<UserVO>> page(UserQueryDTO query) {
        return Result.ok(userService.pageQuery(query));
    }

    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        return Result.ok(userService.pageQuery(
                new UserQueryDTO()
        ).getRecords().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody User user) {
        userService.addUser(user);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "更新用户")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody User user) {
        userService.updateUser(user);
        return Result.ok("更新成功", null);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.ok("删除成功", null);
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/reset-pwd")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.ok("密码已重置为默认密码", null);
    }
}
