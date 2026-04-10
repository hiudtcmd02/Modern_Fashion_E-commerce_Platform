package com.dth.fashionshop.modules.catalog.service;

import com.dth.fashionshop.modules.catalog.dto.response.ProductSuggestionResponse;
import com.dth.fashionshop.modules.catalog.dto.response.storefront.CategoryGuestResponse;
import com.dth.fashionshop.modules.catalog.dto.response.storefront.ProductGuestResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface StorefrontService {

    List<CategoryGuestResponse> getAllActiveCategories();

    List<ProductGuestResponse> getNewestProducts();

    List<ProductGuestResponse> getTopRatedProducts();

    List<ProductSuggestionResponse> getStorefrontSuggestions(String keyword);

    Page<ProductGuestResponse> searchProducts(String keyword, Long categoryId, Long minPrice, Long maxPrice, String sort, int page, int size);
}