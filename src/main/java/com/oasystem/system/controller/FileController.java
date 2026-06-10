package com.oasystem.system.controller;

import com.oasystem.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文件上传控制器
 */
@Slf4j
@Tag(name = "文件上传", description = "通用文件上传/头像上传")
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${oa.upload.path:./uploads}")
    private String uploadPath;

    @Value("${oa.upload.allowed-extensions:jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,txt,zip}")
    private String allowedExtensionsStr;

    /** 允许的图片扩展名 */
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp");

    @Operation(summary = "通用文件上传")
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.badRequest("文件不能为空");
        }

        // 校验文件类型
        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName);
        Set<String> allowed = getAllowedExtensions();
        if (!allowed.contains(ext.toLowerCase())) {
            return Result.badRequest("不支持的文件类型：" + ext + "，允许：" + String.join(",", allowed));
        }

        try {
            String savedName = saveFile(file, ext);
            String url = "/uploads/" + savedName;

            Map<String, String> result = new LinkedHashMap<>();
            result.put("fileName", savedName);
            result.put("originalName", originalName);
            result.put("url", url);
            result.put("size", String.valueOf(file.getSize()));
            log.info("文件上传成功: {} -> {}", originalName, savedName);
            return Result.ok("上传成功", result);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.fail("文件上传失败：" + e.getMessage());
        }
    }

    @Operation(summary = "头像上传（仅限图片）")
    @PostMapping("/upload/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.badRequest("文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName);
        if (ext.isEmpty() || !IMAGE_EXTENSIONS.contains(ext.toLowerCase())) {
            return Result.badRequest("头像仅支持图片格式：" + String.join(",", IMAGE_EXTENSIONS));
        }

        try {
            String savedName = saveFile(file, ext);
            String url = "/uploads/" + savedName;

            Map<String, String> result = new LinkedHashMap<>();
            result.put("fileName", savedName);
            result.put("url", url);
            log.info("头像上传成功: {}", savedName);
            return Result.ok("上传成功", result);
        } catch (IOException e) {
            log.error("头像上传失败", e);
            return Result.fail("头像上传失败：" + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 保存文件到本地
     */
    private String saveFile(MultipartFile file, String ext) throws IOException {
        // 按日期分目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path dir = Paths.get(uploadPath, dateDir);
        Files.createDirectories(dir);

        // 生成唯一文件名
        String savedName = UUID.randomUUID().toString().replace("-", "") + "." + ext.toLowerCase();
        Path targetPath = dir.resolve(savedName);

        // 保存文件
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return dateDir + "/" + savedName;
    }

    /**
     * 获取文件扩展名
     */
    private String getExtension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx + 1) : "";
    }

    /**
     * 获取允许的文件扩展名集合
     */
    private Set<String> getAllowedExtensions() {
        Set<String> set = new LinkedHashSet<>();
        for (String s : allowedExtensionsStr.split(",")) {
            String trimmed = s.trim().toLowerCase();
            if (!trimmed.isEmpty()) {
                set.add(trimmed);
            }
        }
        return set;
    }
}
