package com.dth.fashionshop.modules.catalog.repository;

import com.dth.fashionshop.modules.catalog.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    long countByCategory_IdAndIsDeletedFalse(Long categoryId);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.isDeleted = false")
    long countSellingProducts();

    @Query(value = "SELECT COUNT(*) FROM products p WHERE p.is_deleted = false AND " +
            "(SELECT COALESCE(SUM(v.stock_quantity), 0) FROM product_variants v WHERE v.product_id = p.id AND v.is_active = true) = 0",
            nativeQuery = true)
    long countOutOfStockProducts();

    @Query(value = "SELECT COUNT(*) FROM products p WHERE p.is_deleted = false AND " +
            "(SELECT COALESCE(SUM(v.stock_quantity), 0) FROM product_variants v WHERE v.product_id = p.id AND v.is_active = true) BETWEEN 1 AND 9",
            nativeQuery = true)
    long countLowStockProducts();

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.variants v " +
            "WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.skuCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) ")
    List<Product> suggestProducts(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"variants"})
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v " +
            "WHERE p.isDeleted = false AND v.isActive = true " +
            "ORDER BY p.createdAt DESC")
    List<Product> findTopNewestActiveProducts(Pageable pageable);

    @EntityGraph(attributePaths = {"variants"})
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v " +
            "WHERE p.isDeleted = false AND v.isActive = true AND p.reviewCount >= :minReview " +
            "ORDER BY p.averageRating DESC")
    List<Product> findTopRatedActiveProducts(@Param("minReview") int minReview, Pageable pageable);

    @EntityGraph(attributePaths = {"variants"})
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v " +
            "WHERE p.isDeleted = false AND v.isActive = true " +
            "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> suggestActiveProducts(@Param("keyword") String keyword, Pageable pageable);

    Optional<Product> findBySlugAndIsDeletedFalse(String slug);
}