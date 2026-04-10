package com.dth.fashionshop.modules.catalog.service.impl;

import com.dth.fashionshop.modules.catalog.dto.response.ProductSuggestionResponse;
import com.dth.fashionshop.modules.catalog.dto.response.storefront.CategoryGuestResponse;
import com.dth.fashionshop.modules.catalog.dto.response.storefront.ProductGuestResponse;
import com.dth.fashionshop.modules.catalog.entity.Product;
import com.dth.fashionshop.modules.catalog.entity.ProductVariant;
import com.dth.fashionshop.modules.catalog.repository.CategoryRepository;
import com.dth.fashionshop.modules.catalog.repository.ProductRepository;
import com.dth.fashionshop.modules.catalog.service.StorefrontService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StorefrontServiceImpl implements StorefrontService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    private ProductGuestResponse mapToProductGuestResponse(Product product) {
        LongSummaryStatistics priceStats = product.getVariants().stream()
                .filter(ProductVariant::getIsActive)
                .mapToLong(ProductVariant::getPrice)
                .summaryStatistics();

        return ProductGuestResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .thumbnailUrl(product.getThumbnailUrl())
                .minPrice(priceStats.getCount() > 0 ? priceStats.getMin() : 0L)
                .maxPrice(priceStats.getCount() > 0 ? priceStats.getMax() : 0L)
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryGuestResponse> getAllActiveCategories() {
        return categoryRepository.findAllByIsDeletedFalse().stream()
                .map(c -> CategoryGuestResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .slug(c.getSlug())
                        .thumbnailUrl(c.getThumbnailUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductGuestResponse> getNewestProducts() {
        List<Product> products = productRepository.findTopNewestActiveProducts(PageRequest.of(0, 8));
        return products.stream().map(this::mapToProductGuestResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductGuestResponse> getTopRatedProducts() {
        List<Product> products = productRepository.findTopRatedActiveProducts(3, PageRequest.of(0, 8));
        return products.stream().map(this::mapToProductGuestResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSuggestionResponse> getStorefrontSuggestions(String keyword) {
        if (keyword == null || keyword.trim().length() < 3) {
            return Collections.emptyList();
        }

        List<Product> products = productRepository.suggestActiveProducts(keyword.trim(), PageRequest.of(0, 5));

        return products.stream()
                .map(p -> ProductSuggestionResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .thumbnailUrl(p.getThumbnailUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductGuestResponse> searchProducts(String keyword, Long categoryId, Long minPrice, Long maxPrice, String sort, int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Product> productPage = productRepository.searchStorefrontProducts(
                keyword, categoryId, minPrice, maxPrice, sort, pageRequest);

        return productPage.map(this::mapToProductGuestResponse);
    }
}