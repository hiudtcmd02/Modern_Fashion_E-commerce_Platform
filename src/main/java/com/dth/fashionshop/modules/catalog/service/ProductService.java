package com.dth.fashionshop.modules.catalog.service;

import com.dth.fashionshop.modules.catalog.dto.request.ProductFilterRequest;
import com.dth.fashionshop.modules.catalog.dto.request.ProductRequest;
import com.dth.fashionshop.modules.catalog.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    Page<ProductDetailResponse> getBasicProducts(int page, int size);

    ProductDetailResponse getProductById(Long id);

    ProductDetailResponse createProduct(ProductRequest request,
                                        MultipartFile thumbnail,
                                        List<MultipartFile> images);

    ProductDetailResponse updateProduct(Long id,
                                        ProductRequest request,
                                        MultipartFile thumbnail,
                                        List<MultipartFile> images);

    void deleteProduct(Long id);

    ProductDetailResponse restoreProduct(Long id);

    void deleteProductImage(Long imageId);

    List<VariantResponse> getVariantsByProductId(Long productId);

    VariantResponse updateVariantStock(Long variantId, Integer newStock);

    VariantResponse toggleVariantStatus(Long variantId, Boolean isActive);

    ProductStatsResponse getProductStats();

    Page<ProductListAdminResponse> searchAndFilterAdminProducts(ProductFilterRequest filter, int page, int size);

    List<ProductSuggestionResponse> getAdminProductSuggestions(String keyword);
}