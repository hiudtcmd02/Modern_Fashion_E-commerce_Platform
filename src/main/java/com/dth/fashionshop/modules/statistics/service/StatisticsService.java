package com.dth.fashionshop.modules.statistics.service;

import com.dth.fashionshop.modules.statistics.dto.response.KpiResponse;
import com.dth.fashionshop.modules.statistics.dto.response.ProductSalesResponse;
import com.dth.fashionshop.modules.statistics.dto.response.RevenueChartResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface StatisticsService {

    KpiResponse getDashboardKpis();

    List<RevenueChartResponse> getRevenueChart(String type, Integer days);

    Page<ProductSalesResponse> getProductSales(String timeFilter, String sortDirection, int page, int size);

}