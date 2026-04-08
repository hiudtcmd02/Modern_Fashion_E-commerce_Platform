package com.dth.fashionshop.shared.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final Cloudinary cloudinary;

    // Hàm dùng upload mọi loại ảnh trong hệ thống
    public String uploadImage(MultipartFile file, String folderName) {
        try {
            String publicId = UUID.randomUUID().toString();

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", folderName,
                    "public_id", publicId,
                    "resource_type", "image"
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            log.error("Lỗi khi upload ảnh lên thư mục {} trên Cloudinary", folderName, e);
            throw new RuntimeException("Không thể tải ảnh lên. Vui lòng thử lại sau!");
        }
    }

    // Hàm xóa ảnh trên Cloudinary
    public void deleteImage(String imageUrl) {
        try {
            Pattern pattern = Pattern.compile("upload/(?:v\\d+/)?(.*?)\\.[a-zA-Z0-9]+$");
            Matcher matcher = pattern.matcher(imageUrl);

            if (matcher.find()) {
                String publicId = matcher.group(1);

                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Đã dọn dẹp file rác trên Cloudinary: {}", publicId);
            } else {
                log.warn("Không thể trích xuất public_id từ URL: {}", imageUrl);
            }

        } catch (Exception e) {
            log.error("Lỗi khi xóa file gốc trên Cloudinary: {}", imageUrl, e);
        }
    }
}