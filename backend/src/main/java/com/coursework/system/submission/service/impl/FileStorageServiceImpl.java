package com.coursework.system.submission.service.impl;

import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.submission.config.FileStorageProperties;
import com.coursework.system.submission.entity.FileRecord;
import com.coursework.system.submission.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    private final FileStorageProperties fileStorageProperties;

    public FileStorageServiceImpl(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public FileRecord store(Long submissionId, Long assignmentId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }
        try {
            Path root = Paths.get(fileStorageProperties.getRoot()).toAbsolutePath().normalize();
            Path folder = root.resolve("assignment-" + assignmentId).resolve(LocalDate.now().toString()).normalize();
            Files.createDirectories(folder);

            String originalName = file.getOriginalFilename() == null ? "upload.bin" : file.getOriginalFilename();
            String safeName = originalName.replaceAll("[\\\\/:*?\"<>|]", "_");
            String storedName = UUID.randomUUID().toString().replace("-", "") + "_" + safeName;
            Path target = folder.resolve(storedName).normalize();
            if (!target.startsWith(root)) {
                throw new BusinessException(400, "文件路径非法");
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream inputStream = file.getInputStream();
                 DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest)) {
                Files.copy(digestInputStream, target);
            }

            FileRecord record = new FileRecord();
            record.setSubmissionId(submissionId);
            record.setFileName(originalName);
            record.setFilePath(target.toString());
            record.setFileSize(Files.size(target));
            record.setFileHash(toHex(digest.digest()));
            record.setUploadedAt(LocalDateTime.now());
            return record;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(500, "文件保存失败：" + exception.getMessage());
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }
}
