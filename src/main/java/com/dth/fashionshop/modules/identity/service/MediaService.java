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
            // 1. Tạo tên file ngẫu nhiên để không bị trùng lặp
            String publicId = "avatar_" + UUID.randomUUID().toString();

            // 2. Cấu hình các tham số đẩy lên Cloudinary
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", "fashionshop/avatars", // Tự động tạo thư mục này trên Cloudinary
                    "public_id", publicId,           // Tên file
                    "resource_type", "image"         // Chỉ nhận ảnh
            );

            // 3. Thực hiện đẩy file (getBytes) lên mây
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            // 4. Lấy cái Link ảnh an toàn (https) trả về
            String imageUrl = uploadResult.get("secure_url").toString();
            log.info("Đã upload thành công avatar lên Cloudinary: {}", imageUrl);

            return imageUrl;

        } catch (IOException e) {
            log.error("Lỗi khi upload ảnh lên Cloudinary", e);
            throw new RuntimeException("Không thể tải ảnh lên. Vui lòng thử lại sau!");
        }
    }
}