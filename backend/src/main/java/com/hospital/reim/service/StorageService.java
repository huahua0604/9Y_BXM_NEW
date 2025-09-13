package com.hospital.reim.service;

import com.hospital.reim.entity.Attachment;
import com.hospital.reim.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import java.util.Set;


// @Service
// @RequiredArgsConstructor
// public class StorageService {

//     @Value("${app.upload-dir:data/uploads}")
//     private String uploadDir;

//     private static final Tika TIKA = new Tika();

//     /** 保存 PDF，返回存储信息 */
//     public StoredFile savePdf(MultipartFile file) {
//         if (file == null || file.isEmpty()) throw new BadRequestException("请选择要上传的文件");
//         String contentType = detectType(file);
//         if (!"application/pdf".equalsIgnoreCase(contentType)) {
//             throw new BadRequestException("仅支持 PDF 文件");
//         }

//         try {
//             Files.createDirectories(Path.of(uploadDir));

//             String ext = FilenameUtils.getExtension(file.getOriginalFilename());
//             if (ext == null || ext.isBlank()) ext = "pdf";
//             String stored = UUID.randomUUID().toString().replace("-", "") + "." + ext;

//             Path target = Path.of(uploadDir, stored);
//             try (InputStream in = file.getInputStream()) {
//                 Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
//             }

//             String sha256 = sha256Hex(target);
//             long size = Files.size(target);

//             return new StoredFile(file.getOriginalFilename(), stored, uploadDir, contentType, size, sha256);
//         } catch (Exception e) {
//             throw new RuntimeException("保存文件失败", e);
//         }
//     }

    

//     public Path resolvePath(Attachment att) {
//         return Path.of(att.getStoragePath(), att.getStoredFilename());
//     }

//     private String detectType(MultipartFile file) {
//         try (InputStream in = file.getInputStream()) {
//             return TIKA.detect(in, file.getOriginalFilename());
//         } catch (Exception e) {
//             return file.getContentType();
//         }
//     }

//     private String sha256Hex(Path path) throws Exception {
//         MessageDigest md = MessageDigest.getInstance("SHA-256");
//         byte[] data = Files.readAllBytes(path);
//         return HexFormat.of().formatHex(md.digest(data));
//     }

//     /** 存储结果描述（Java 21 record） */
//     public record StoredFile(
//             String originalFilename,
//             String storedFilename,
//             String storagePath,
//             String contentType,
//             long size,
//             String sha256
//     ) {}
// }

// ... 省略 package / import / @Service 等

@Service
@RequiredArgsConstructor
public class StorageService {

    @Value("${app.upload-dir:data/uploads}")
    private String uploadDir;

    private static final Tika TIKA = new Tika();

    // 允许的 MIME 类型白名单
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
            // 如需放开更多： "image/gif", "image/heic"
    );

    // 选择性：限制大小（例如 10MB）
    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

    /** 向后兼容旧调用：仍然可用 */
    public StoredFile savePdf(MultipartFile file) {
        return saveDocumentOrImage(file);
    }

    /** 新：保存 PDF 或 图片 */
    public StoredFile saveDocumentOrImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("请选择要上传的文件");
        if (file.getSize() > MAX_SIZE_BYTES) throw new BadRequestException("文件过大，最大 10MB");

        String detected = detectType(file);
        if (!ALLOWED_TYPES.contains(detected)) {
            throw new BadRequestException("仅支持 PDF、JPG、PNG 格式");
        }

        try {
            Files.createDirectories(Path.of(uploadDir));

            // 根据 MIME 决定扩展名（若原文件无扩展名或不可信）
            String ext = switch (detected) {
                case "application/pdf" -> "pdf";
                case "image/jpeg" -> "jpg";
                case "image/png" -> "png";
                default -> "bin";
            };

            // 尽量从原文件名取扩展名；没有就用上面推断
            String originalName = file.getOriginalFilename();
            String originalExt = FilenameUtils.getExtension(originalName);
            if (originalExt == null || originalExt.isBlank()) originalExt = ext;

            String stored = UUID.randomUUID().toString().replace("-", "") + "." + originalExt;
            Path target = Path.of(uploadDir, stored);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            String sha256 = sha256Hex(target);
            long size = Files.size(target);

            return new StoredFile(
                    originalName != null ? originalName : stored,
                    stored,
                    uploadDir,
                    detected,
                    size,
                    sha256
            );
        } catch (Exception e) {
            throw new RuntimeException("保存文件失败", e);
        }
    }

    public Path resolvePath(Attachment att) {
        return Path.of(att.getStoragePath(), att.getStoredFilename());
    }

    private String detectType(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            // Tika 猜测（优先）；若失败，退回浏览器给的 contentType
            String t = TIKA.detect(in, file.getOriginalFilename());
            return (t != null && !t.isBlank()) ? t : file.getContentType();
        } catch (Exception e) {
            return file.getContentType();
        }
    }

    private String sha256Hex(Path path) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] data = Files.readAllBytes(path);
        return HexFormat.of().formatHex(md.digest(data));
    }

    public record StoredFile(
            String originalFilename,
            String storedFilename,
            String storagePath,
            String contentType,
            long size,
            String sha256
    ) {}
}
