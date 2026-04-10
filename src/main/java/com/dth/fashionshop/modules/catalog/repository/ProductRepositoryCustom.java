package com.dth.fashionshop.modules.catalog.repository;

import com.dth.fashionshop.modules.catalog.dto.request.ProductFilterRequest;
import com.dth.fashionshop.modules.catalog.dto.response.ProductListAdminResponse;
import com.dth.fashionshop.modules.catalog.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
}