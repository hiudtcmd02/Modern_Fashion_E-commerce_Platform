package com.dth.fashionshop.modules.catalog.repository;

import com.dth.fashionshop.modules.catalog.dto.request.ProductFilterRequest;
import com.dth.fashionshop.modules.catalog.dto.response.ProductListAdminResponse;
import com.dth.fashionshop.modules.catalog.entity.Product;
import com.dth.fashionshop.modules.statistics.dto.response.ProductSalesResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ProductRepositoryCustom {
    Page<ProductListAdminResponse> searchAndFilterAdmin(ProductFilterRequest filter, Pageable pageable);

    Page<Product> searchStorefrontProducts(
            String keyword,
            Long categoryId,
            Long minPrice,
            Long maxPrice,
            String sort,
            Pageable pageable
    );

    Page<ProductSalesResponse> getProductSalesAnalytics(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String sortDirection,
            Pageable pageable
    );
}