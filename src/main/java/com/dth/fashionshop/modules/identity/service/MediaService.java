package com.dth.fashionshop.modules.identity.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final Cloudinary cloudinary;

    public String uploadAvatar(MultipartFile file) {
        try {
            String publicId = "avatar_" + UUID.randomUUID().toString();

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", "fashionshop/avatars",
                    "public_id", publicId,
                    "resource_type", "image"
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            String imageUrl = uploadResult.get("secure_url").toString();
            log.info("Đã upload thành công avatar lên Cloudinary: {}", imageUrl);

            return imageUrl;

        } catch (IOException e) {
            log.error("Lỗi khi upload ảnh lên Cloudinary", e);
            throw new RuntimeException("Không thể tải ảnh lên. Vui lòng thử lại sau!");
        }
    }
}