package org.example.blog.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.blog.exception.BadRequestException;
import org.example.blog.service.UploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public String upload(MultipartFile file){
        // 1. 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.contains("image/")) {
            throw new BadRequestException("只能上传图片文件");
        }

        // 2. 生成文件名
        String suffix = "." + StringUtils.getFilenameExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + suffix;

        // 3. 保存文件
        try {
            Files.createDirectories(Paths.get(uploadPath));
            file.transferTo(new File(uploadPath, fileName));
        } catch (IOException e) {
            throw new RuntimeException("上传失败", e);
        }

        return "/upload/" + fileName;
    }
}
