package com.dth.fashionshop.modules.catalog.repository;

import com.dth.fashionshop.modules.catalog.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @Query("SELECT c FROM Category c WHERE " +
            "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:isDeleted IS NULL OR c.isDeleted = :isDeleted)")
    Page<Category> searchAndFilterCategories(@Param("keyword") String keyword,
                                             @Param("isDeleted") Boolean isDeleted,
                                             Pageable pageable);
}