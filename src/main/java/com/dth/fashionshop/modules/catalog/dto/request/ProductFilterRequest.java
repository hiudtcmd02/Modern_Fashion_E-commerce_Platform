package com.dth.fashionshop.modules.catalog.dto.request;

import lombok.Data;

@Data
public class ProductFilterRequest {
    private String keyword;
    private Long categoryId;
    private String status;
    private String inventoryStatus;
}