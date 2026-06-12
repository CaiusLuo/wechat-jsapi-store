package com.example.wechatstore.modules.file.controller;

import com.example.wechatstore.common.result.Result;
import com.example.wechatstore.modules.file.service.FileUploadService;
import com.example.wechatstore.modules.file.vo.FileUploadVO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/files")
public class AdminFileController {

    private final FileUploadService fileUploadService;

    public AdminFileController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<FileUploadVO> upload(@RequestPart("file") MultipartFile file) {
        return Result.ok(fileUploadService.uploadImage(file));
    }
}
