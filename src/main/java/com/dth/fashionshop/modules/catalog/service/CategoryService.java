package com.dth.fashionshop.modules.catalog.service;

import com.dth.fashionshop.modules.catalog.dto.request.CategoryRequest;
import com.dth.fashionshop.modules.catalog.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface CategoryService {

    Page<CategoryResponse> getAllCategories(String keyword, Boolean isDeleted, int page, int size);

    CategoryResponse getCategoryById(Long id);

    CategoryResponse createCategory(CategoryRequest request, MultipartFile file);

    CategoryResponse updateCategory(Long id, CategoryRequest request, MultipartFile file);

    void deleteCategory(Long id);
}