package com.oasystem.system.controller;

import com.oasystem.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 应用根路径健康检查与提示
 */
@Tag(name = "应用", description = "应用根路径与状态")
@RestController
@RequestMapping("/")
public class HomeController {

    @Operation(summary = "应用运行状态")
    @GetMapping
    public Result<String> index() {
        return Result.ok("OA System 后端运行中，访问 API 文档: /doc.html");
    }
}

