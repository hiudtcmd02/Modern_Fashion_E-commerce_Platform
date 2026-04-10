package com.dth.fashionshop.modules.catalog.repository.impl;

import com.dth.fashionshop.modules.catalog.dto.request.ProductFilterRequest;
import com.dth.fashionshop.modules.catalog.dto.response.ProductListAdminResponse;
import com.dth.fashionshop.modules.catalog.entity.Product;
import com.dth.fashionshop.modules.catalog.repository.ProductRepositoryCustom;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private final EntityManager em;

    @Override
    public Page<ProductListAdminResponse> searchAndFilterAdmin(ProductFilterRequest filter, Pageable pageable) {

        StringBuilder sql = new StringBuilder(
                "SELECT p.id as id, p.name as name, p.thumbnailUrl as thumbnailUrl, c.name as categoryName, " +
                        "MIN(v.price) as minPrice, MAX(v.price) as maxPrice, " +
                        "SUM(v.stockQuantity) as totalStock, " +
                        "p.isDeleted as isDeleted, p.createdAt as createdAt, " +
                        "SUM(CASE WHEN v.stockQuantity = 0 THEN 1 ELSE 0 END) as outOfStockSkuCount, " +
                        "SUM(CASE WHEN v.stockQuantity > 0 AND v.stockQuantity < 10 THEN 1 ELSE 0 END) as lowStockSkuCount " +
                        "FROM Product p " +
                        "JOIN p.category c " +
                        "LEFT JOIN p.variants v ON v.isActive = true " +
                        "WHERE 1=1 "
        );

        Map<String, Object> params = new HashMap<>();

        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            sql.append("AND (LOWER(p.name) LIKE :keyword OR LOWER(v.skuCode) LIKE :keyword) ");
            params.put("keyword", "%" + filter.getKeyword().trim().toLowerCase() + "%");
        }

        if (filter.getCategoryId() != null) {
            sql.append("AND c.id = :categoryId ");
            params.put("categoryId", filter.getCategoryId());
        }

        if ("ACTIVE".equalsIgnoreCase(filter.getStatus())) {
            sql.append("AND p.isDeleted = false ");
        } else if ("HIDDEN".equalsIgnoreCase(filter.getStatus())) {
            sql.append("AND p.isDeleted = true ");
        }

        sql.append("GROUP BY p.id, p.name, p.thumbnailUrl, c.name, p.isDeleted, p.createdAt ");

        if (filter.getInventoryStatus() != null) {
            switch (filter.getInventoryStatus().toUpperCase()) {
                case "OUT_OF_STOCK":
                    sql.append("HAVING SUM(v.stockQuantity) = 0 OR SUM(v.stockQuantity) IS NULL ");
                    break;
                case "HAS_OUT_OF_STOCK_SKU":
                    sql.append("HAVING SUM(CASE WHEN v.stockQuantity = 0 THEN 1 ELSE 0 END) > 0 ");
                    break;
                case "HAS_LOW_STOCK_SKU":
                    sql.append("HAVING SUM(CASE WHEN v.stockQuantity > 0 AND v.stockQuantity < 10 THEN 1 ELSE 0 END) > 0 ");
                    break;
            }
        }

        sql.append("ORDER BY p.id DESC");

        Query query = em.createQuery(sql.toString(), Tuple.class);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        String countSql = sql.toString().replaceFirst("SELECT .*? FROM", "SELECT p.id FROM");
        Query countQuery = em.createQuery(countSql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long totalElements = countQuery.getResultList().size();

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Tuple> tuples = query.getResultList();

        List<ProductListAdminResponse> content = tuples.stream().map(t -> {
            Long totalStock = t.get("totalStock", Long.class);
            Long outOfStockCount = t.get("outOfStockSkuCount", Long.class);
            Long lowStockCount = t.get("lowStockSkuCount", Long.class);

            return ProductListAdminResponse.builder()
                    .id(t.get("id", Long.class))
                    .name(t.get("name", String.class))
                    .thumbnailUrl(t.get("thumbnailUrl", String.class))
                    .categoryName(t.get("categoryName", String.class))
                    .minPrice(t.get("minPrice", Long.class))
                    .maxPrice(t.get("maxPrice", Long.class))
                    .totalStock(totalStock != null ? totalStock : 0L)
                    .isDeleted(t.get("isDeleted", Boolean.class))
                    .createdAt(t.get("createdAt", LocalDateTime.class))
                    .hasOutOfStockSku(outOfStockCount != null && outOfStockCount > 0)
                    .hasLowStockSku(lowStockCount != null && lowStockCount > 0)
                    .build();
        }).collect(Collectors.toList());

        return new PageImpl<>(content, pageable, totalElements);
    }

    @Override
    public Page<Product> searchStorefrontProducts(String keyword, Long categoryId, Long minPrice, Long maxPrice, String sort, Pageable pageable) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p JOIN p.variants v WHERE p.isDeleted = false AND v.isActive = true ");
        StringBuilder countBuilder = new StringBuilder("SELECT COUNT(DISTINCT p.id) FROM Product p JOIN p.variants v WHERE p.isDeleted = false AND v.isActive = true ");

        Map<String, Object> params = new HashMap<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            jpql.append("AND LOWER(p.name) LIKE :keyword ");
            countBuilder.append("AND LOWER(p.name) LIKE :keyword ");
            params.put("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }

        if (categoryId != null) {
            jpql.append("AND p.category.id = :categoryId ");
            countBuilder.append("AND p.category.id = :categoryId ");
            params.put("categoryId", categoryId);
        }

        if (minPrice != null) {
            jpql.append("AND v.price >= :minPrice ");
            countBuilder.append("AND v.price >= :minPrice ");
            params.put("minPrice", minPrice);
        }
        if (maxPrice != null) {
            jpql.append("AND v.price <= :maxPrice ");
            countBuilder.append("AND v.price <= :maxPrice ");
            params.put("maxPrice", maxPrice);
        }

        jpql.append("GROUP BY p.id ");

        if (sort != null) {
            switch (sort.toLowerCase()) {
                case "price_asc":
                    jpql.append("ORDER BY MIN(v.price) ASC ");
                    break;
                case "price_desc":
                    jpql.append("ORDER BY MAX(v.price) DESC ");
                    break;
                case "newest":
                    jpql.append("ORDER BY p.createdAt DESC ");
                    break;
                case "oldest":
                    jpql.append("ORDER BY p.createdAt ASC ");
                    break;
                default:
                    jpql.append("ORDER BY p.createdAt DESC ");
            }
        } else {
            jpql.append("ORDER BY p.createdAt DESC ");
        }

        TypedQuery<Product> query = em.createQuery(jpql.toString(), Product.class);
        TypedQuery<Long> countQuery = em.createQuery(countBuilder.toString(), Long.class);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Product> products = query.getResultList();
        Long totalElements = countQuery.getSingleResult();

        return new PageImpl<>(products, pageable, totalElements != null ? totalElements : 0);
    }
}