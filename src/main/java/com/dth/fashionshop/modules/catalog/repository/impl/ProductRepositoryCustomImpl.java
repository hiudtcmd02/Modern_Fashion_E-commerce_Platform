package com.dth.fashionshop.modules.catalog.repository.impl;

import com.dth.fashionshop.modules.catalog.dto.request.ProductFilterRequest;
import com.dth.fashionshop.modules.catalog.dto.response.ProductListAdminResponse;
import com.dth.fashionshop.modules.catalog.repository.ProductRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
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
}