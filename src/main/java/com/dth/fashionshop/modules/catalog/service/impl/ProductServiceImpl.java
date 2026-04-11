package com.dth.fashionshop.modules.catalog.service.impl;

import com.dth.fashionshop.modules.catalog.dto.request.ProductFilterRequest;
import com.dth.fashionshop.modules.catalog.dto.request.ProductRequest;
import com.dth.fashionshop.modules.catalog.dto.request.VariantRequest;
import com.dth.fashionshop.modules.catalog.dto.response.*;
import com.dth.fashionshop.modules.catalog.entity.Category;
import com.dth.fashionshop.modules.catalog.entity.Product;
import com.dth.fashionshop.modules.catalog.entity.ProductImage;
import com.dth.fashionshop.modules.catalog.entity.ProductVariant;
import com.dth.fashionshop.modules.catalog.repository.CategoryRepository;
import com.dth.fashionshop.modules.catalog.repository.ProductImageRepository;
import com.dth.fashionshop.modules.catalog.repository.ProductRepository;
import com.dth.fashionshop.modules.catalog.repository.ProductVariantRepository;
import com.dth.fashionshop.modules.catalog.service.ProductService;
import com.dth.fashionshop.shared.media.MediaService;
import com.dth.fashionshop.shared.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CategoryRepository categoryRepository;
    private final MediaService mediaService;
    private final ProductImageRepository productImageRepository;

    private VariantResponse mapToVariantResponse(ProductVariant variant) {
        return VariantResponse.builder()
                .id(variant.getId())
                .skuCode(variant.getSkuCode())
                .variantName(variant.getVariantName())
                .price(variant.getPrice())
                .stockQuantity(variant.getStockQuantity())
                .isActive(variant.getIsActive())
                .build();
    }

    private ImageResponse mapToImageResponse(ProductImage image) {
        return ImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .build();
    }

    private ProductDetailResponse mapToDetailResponse(Product product) {
        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .thumbnailUrl(product.getThumbnailUrl())
                .description(product.getDescription())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .isDeleted(product.getIsDeleted())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .variants(product.getVariants().stream().map(this::mapToVariantResponse).collect(Collectors.toList()))
                .images(product.getImages().stream().map(this::mapToImageResponse).collect(Collectors.toList()))
                .build();
    }

    //Hàm lấy danh sách Product và phân trang
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDetailResponse> getBasicProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::mapToDetailResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        return mapToDetailResponse(product);
    }

    @Override
    @Transactional
    public ProductDetailResponse createProduct(ProductRequest request, MultipartFile thumbnail, List<MultipartFile> images) {

        String safeSlug = StringUtils.generateSlug(request.getSlug());
        if (productRepository.existsBySlug(safeSlug)) {
            throw new RuntimeException("Chuỗi định danh (slug) đã tồn tại!");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));

        Set<String> checkingSkus = new HashSet<>();
        for (VariantRequest vReq : request.getVariants()) {
            String safeSku = StringUtils.normalizeCode(vReq.getSkuCode());
            if (!checkingSkus.add(safeSku)) {
                throw new RuntimeException("Mã SKU [" + safeSku + "] bị trùng lặp bên trong danh sách gửi lên!");
            }
            if (variantRepository.existsBySkuCode(safeSku)) {
                throw new RuntimeException("Mã SKU [" + safeSku + "] đã tồn tại trên hệ thống!");
            }
        }

        if (thumbnail == null || thumbnail.isEmpty()) {
            throw new RuntimeException("Sản phẩm bắt buộc phải có ảnh đại diện!");
        }
        String thumbnailUrl = mediaService.uploadImage(thumbnail, "fashionshop/products/thumbnails");

        Product product = Product.builder()
                .name(request.getName().trim())
                .slug(safeSlug)
                .category(category)
                .thumbnailUrl(thumbnailUrl)
                .description(request.getDescription())
                .build();

        for (VariantRequest vReq : request.getVariants()) {
            String safeSku = StringUtils.normalizeCode(vReq.getSkuCode());

            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .skuCode(safeSku)
                    .variantName(vReq.getVariantName())
                    .price(vReq.getPrice())
                    .stockQuantity(vReq.getStockQuantity())
                    .isActive(vReq.getIsActive())
                    .build();

            product.getVariants().add(variant);
        }

        if (images != null && !images.isEmpty()) {
            for (MultipartFile imgFile : images) {
                if (!imgFile.isEmpty()) {
                    String imgUrl = mediaService.uploadImage(imgFile, "fashionshop/products/gallery");

                    ProductImage productImage = ProductImage.builder()
                            .product(product)
                            .imageUrl(imgUrl)
                            .build();

                    product.getImages().add(productImage);
                }
            }
        }

        Product savedProduct = productRepository.save(product);
        log.info("Admin đã thêm mới thành công sản phẩm: {} (Kèm {} phân loại của sản phẩm)", savedProduct.getName(), savedProduct.getVariants().size());

        return mapToDetailResponse(savedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        if (product.getIsDeleted()) {
            throw new RuntimeException("Sản phẩm này đã bị xóa rồi!");
        }

        product.setIsDeleted(true);
        product.getVariants().forEach(variant -> variant.setIsActive(false));

        productRepository.save(product);
        log.info("Admin đã xóa mềm Sản phẩm: {}", product.getName());
    }

    @Override
    @Transactional
    public ProductDetailResponse restoreProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        if (!product.getIsDeleted()) {
            throw new RuntimeException("Sản phẩm vẫn đang hoạt động, không cần khôi phục!");
        }

        product.setIsDeleted(false);
        product.getVariants().forEach(variant -> variant.setIsActive(true));

        Product restoredProduct = productRepository.save(product);
        log.info("Admin đã khôi phục Sản phẩm: {}", restoredProduct.getName());

        return mapToDetailResponse(restoredProduct);
    }

    @Override
    @Transactional
    public ProductDetailResponse updateProduct(Long id, ProductRequest request, MultipartFile thumbnail, List<MultipartFile> images) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        String safeSlug = StringUtils.generateSlug(request.getSlug());
        if (productRepository.existsBySlugAndIdNot(safeSlug, id)) {
            throw new RuntimeException("Chuỗi định danh (slug) đã tồn tại ở một sản phẩm khác!");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));

        product.setName(request.getName().trim());
        product.setSlug(safeSlug);
        product.setCategory(category);
        product.setDescription(request.getDescription());

        if (thumbnail != null && !thumbnail.isEmpty()) {
            if (product.getThumbnailUrl() != null) {
                mediaService.deleteImage(product.getThumbnailUrl());
            }
            product.setThumbnailUrl(mediaService.uploadImage(thumbnail, "fashionshop/products/thumbnails"));
        }

        if (images != null && !images.isEmpty()) {
            for (MultipartFile imgFile : images) {
                if (!imgFile.isEmpty()) {
                    ProductImage productImage = ProductImage.builder()
                            .product(product)
                            .imageUrl(mediaService.uploadImage(imgFile, "fashionshop/products/gallery"))
                            .build();
                    product.getImages().add(productImage);
                }
            }
        }

        //Đưa các biển thể mới vào HashMap
        Map<String, VariantRequest> incomingVariantMap = new HashMap<>();
        for (VariantRequest vReq : request.getVariants()) {
            String safeSku = StringUtils.normalizeCode(vReq.getSkuCode());
            if (incomingVariantMap.containsKey(safeSku)) {
                throw new RuntimeException("Mã SKU [" + safeSku + "] bị trùng lặp bên trong danh sách gửi lên!");
            }
            incomingVariantMap.put(safeSku, vReq);
        }

        // Duyệt qua các biến thể cũ trong DB
        for (ProductVariant existingVariant : product.getVariants()) {
            String existingSku = existingVariant.getSkuCode().toUpperCase();

            // Cập nhật những variant tồn tại trong cả DB và danh sách gửi lên
            if (incomingVariantMap.containsKey(existingSku)) {
                VariantRequest incomingData = incomingVariantMap.get(existingSku);
                existingVariant.setVariantName(incomingData.getVariantName());
                existingVariant.setPrice(incomingData.getPrice());
                existingVariant.setStockQuantity(incomingData.getStockQuantity());
                existingVariant.setIsActive(incomingData.getIsActive());

                incomingVariantMap.remove(existingSku);
            } else {
                throw new RuntimeException("Dữ liệu gửi lên không hợp lệ: Bị mất thông tin của biến thể cũ [" + existingSku + "].");
            }
        }

        // Thêm mới những biến thể còn lại trong danh sách gửi lên
        for (VariantRequest newVariantReq : incomingVariantMap.values()) {
            String safeSku = StringUtils.normalizeCode(newVariantReq.getSkuCode());

            if (variantRepository.existsBySkuCode(safeSku)) {
                throw new RuntimeException("Mã SKU [" + safeSku + "] đã được sử dụng bởi một sản phẩm khác trên hệ thống!");
            }

            ProductVariant newVariant = ProductVariant.builder()
                    .product(product)
                    .skuCode(safeSku)
                    .variantName(newVariantReq.getVariantName())
                    .price(newVariantReq.getPrice())
                    .stockQuantity(newVariantReq.getStockQuantity())
                    .isActive(newVariantReq.getIsActive())
                    .build();
            product.getVariants().add(newVariant);
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Admin đã cập nhật thành công Sản phẩm: {} (ID: {})", updatedProduct.getName(), updatedProduct.getId());

        return mapToDetailResponse(updatedProduct);
    }

    // Xóa ảnh phụ
    @Override
    @Transactional
    public void deleteProductImage(Long imageId) {

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hình ảnh phụ này để xóa!"));

        mediaService.deleteImage(image.getImageUrl());

        productImageRepository.delete(image);
        log.info("Admin đã xóa ảnh phụ có ID: {}", imageId);
    }

    // Hàm lấy danh sách phân loại (biến thể) của sản phẩm
    @Override
    @Transactional(readOnly = true)
    public List<VariantResponse> getVariantsByProductId(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        return product.getVariants().stream()
                .map(this::mapToVariantResponse)
                .collect(Collectors.toList());
    }

    // Hàm cập nhật nhanh số lượng tồn kho của phân loại (biến thể)
    @Override
    @Transactional
    public VariantResponse updateVariantStock(Long variantId, Integer newStock) {
        if (newStock == null || newStock < 0) {
            throw new RuntimeException("Số lượng tồn kho không hợp lệ!");
        }

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã SKU này!"));

        variant.setStockQuantity(newStock);
        ProductVariant savedVariant = variantRepository.save(variant);

        log.info("Admin đã cập nhật tồn kho biến thể {} thành {}", variant.getSkuCode(), newStock);
        return mapToVariantResponse(savedVariant);
    }

    // Hàm bật/tắt nhanh trạng thái của phân loại (biến thể)
    @Override
    @Transactional
    public VariantResponse toggleVariantStatus(Long variantId, Boolean isActive) {
        if (isActive == null) {
            throw new RuntimeException("Trạng thái hoạt động không hợp lệ!");
        }

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã SKU này!"));

        variant.setIsActive(isActive);
        ProductVariant savedVariant = variantRepository.save(variant);

        log.info("Admin đã thay đổi trạng thái biến thể {} thành {}", variant.getSkuCode(), isActive);
        return mapToVariantResponse(savedVariant);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductStatsResponse getProductStats() {
        return ProductStatsResponse.builder()
                .totalSelling(productRepository.countSellingProducts())
                .totalOutOfStock(productRepository.countOutOfStockProducts())
                .totalLowStock(productRepository.countLowStockProducts())
                .build();
    }

    // Hàm tìm kiếm, lọc và phân trang danh sách sản phẩm
    @Override
    @Transactional(readOnly = true)
    public Page<ProductListAdminResponse> searchAndFilterAdminProducts(ProductFilterRequest filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.searchAndFilterAdmin(filter, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSuggestionResponse> getAdminProductSuggestions(String keyword) {
        if (keyword == null || keyword.trim().length() < 3) {
            return Collections.emptyList();
        }

        Pageable limit = PageRequest.of(0, 3);

        List<Product> products = productRepository.suggestProducts(keyword.trim(), limit);

        return products.stream()
                .map(p -> ProductSuggestionResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .thumbnailUrl(p.getThumbnailUrl())
                        .build())
                .collect(Collectors.toList());
    }
}