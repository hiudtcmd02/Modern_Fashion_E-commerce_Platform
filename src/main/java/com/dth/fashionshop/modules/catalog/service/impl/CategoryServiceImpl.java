package com.dth.fashionshop.modules.catalog.service.impl;

import com.dth.fashionshop.modules.catalog.dto.request.CategoryRequest;
import com.dth.fashionshop.modules.catalog.dto.response.CategoryResponse;
import com.dth.fashionshop.modules.catalog.entity.Category;
import com.dth.fashionshop.modules.catalog.repository.CategoryRepository;
import com.dth.fashionshop.modules.catalog.repository.ProductRepository;
import com.dth.fashionshop.modules.catalog.service.CategoryService;
import com.dth.fashionshop.shared.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dth.fashionshop.shared.media.MediaService;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final MediaService mediaService;

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .thumbnailUrl(category.getThumbnailUrl())
                .description(category.getDescription())
                .isDeleted(category.getIsDeleted())
                .build();
    }

    @Override
    public Page<CategoryResponse> getAllCategories(String keyword, Boolean isDeleted, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Category> categoryPage = categoryRepository.searchAndFilterCategories(keyword, isDeleted, pageable);
        return categoryPage.map(this::mapToResponse);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, MultipartFile file) {
        String safeSlug = StringUtils.generateSlug(request.getSlug());

        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên danh mục đã tồn tại!");
        }
        if (categoryRepository.existsBySlug(safeSlug)) {
            throw new RuntimeException("Chuỗi định danh (slug) đã tồn tại!");
        }

        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            imageUrl = mediaService.uploadImage(file, "fashionshop/categories");
        }

        Category newCategory = Category.builder()
                .name(request.getName().trim())
                .slug(safeSlug)
                .thumbnailUrl(imageUrl)
                .description(request.getDescription())
                .build();

        Category savedCategory = categoryRepository.save(newCategory);

        log.info("Admin đã thêm mới thành công danh mục: {} (ID: {})", savedCategory.getName(), savedCategory.getId());

        return mapToResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request, MultipartFile file) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        String safeSlug = StringUtils.generateSlug(request.getSlug());

        if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new RuntimeException("Tên danh mục đã bị trùng với danh mục khác!");
        }
        if (categoryRepository.existsBySlugAndIdNot(safeSlug, id)) {
            throw new RuntimeException("Chuỗi định danh đã bị trùng với danh mục khác!");
        }

        if (file != null && !file.isEmpty()) {
            if (category.getThumbnailUrl() != null) {
                mediaService.deleteImage(category.getThumbnailUrl());
            }
            String newImageUrl = mediaService.uploadImage(file, "fashionshop/categories");
            category.setThumbnailUrl(newImageUrl);
        }

        category.setName(request.getName().trim());
        category.setSlug(safeSlug);
        category.setDescription(request.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        log.info("Admin đã cập nhật thành công danh mục: {} (ID: {})", updatedCategory.getName(), updatedCategory.getId());

        return mapToResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        long activeProductsCount = productRepository.countByCategory_IdAndIsDeletedFalse(id);

        if (activeProductsCount > 0) {
            log.warn("Cảnh báo an ninh: Admin cố tình xóa danh mục {} đang có {} sản phẩm", category.getName(), activeProductsCount);
            throw new RuntimeException("Không thể xóa! Danh mục này đang chứa " + activeProductsCount + " sản phẩm. Vui lòng chuyển hết sản phẩm sang danh mục khác trước khi xóa.");
        }

        if (category.getIsDeleted()) {
            throw new RuntimeException("Danh mục này đã được xóa, không cần xóa thêm nữa!");
        }

        category.setIsDeleted(true);
        categoryRepository.save(category);
        log.info("Admin đã xóa mềm thành công danh mục: {} (ID: {})", category.getName(), category.getId());
    }

    @Override
    @Transactional
    public CategoryResponse restoreCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        if (!category.getIsDeleted()) {
            throw new RuntimeException("Danh mục này vẫn đang hoạt động, không cần khôi phục!");
        }

        category.setIsDeleted(false);
        Category restoredCategory = categoryRepository.save(category);

        log.info("Admin đã khôi phục thành công danh mục: {} (ID: {})", restoredCategory.getName(), restoredCategory.getId());

        return mapToResponse(restoredCategory);
    }
}