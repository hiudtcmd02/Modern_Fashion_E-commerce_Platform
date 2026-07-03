package com.dth.fashionshop.modules.statistics.service.impl;

import com.dth.fashionshop.modules.catalog.service.ProductService;
import com.dth.fashionshop.modules.identity.service.AdminUserService;
import com.dth.fashionshop.modules.order.service.AdminOrderService;
import com.dth.fashionshop.modules.statistics.dto.response.KpiResponse;
import com.dth.fashionshop.modules.statistics.dto.response.ProductSalesResponse;
import com.dth.fashionshop.modules.statistics.dto.response.RevenueChartResponse;
import com.dth.fashionshop.modules.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    private final AdminOrderService adminOrderService;
    private final AdminUserService adminUserService;
    private final ProductService productService;

    private final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    public KpiResponse getDashboardKpis() {
        LocalDateTime now = LocalDateTime.now(ZONE_VN);

        LocalDateTime startOfThisMonth = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        LocalDateTime endOfThisMonth = now;

        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = now.minusMonths(1);

        Long currentRevenue = adminOrderService.calculateTotalRevenue(startOfThisMonth, endOfThisMonth);
        Long lastRevenue = adminOrderService.calculateTotalRevenue(startOfLastMonth, endOfLastMonth);

        Double growth = 0.0;
        if (lastRevenue > 0) {
            growth = ((double) (currentRevenue - lastRevenue) / lastRevenue) * 100;
            growth = Math.round(growth * 100.0) / 100.0;
        } else if (currentRevenue > 0) {
            growth = 100.0;
        }

        Long totalCompletedOrders = adminOrderService.countCompletedOrders(startOfThisMonth, endOfThisMonth);

        Long newCustomers = adminUserService.countNewCustomers(startOfThisMonth, endOfThisMonth);

        Long lowStockSkus = productService.countLowStockVariants();

        return KpiResponse.builder()
                .totalRevenue(currentRevenue)
                .revenueGrowthPercentage(growth)
                .totalCompletedOrders(totalCompletedOrders)
                .newCustomers(newCustomers)
                .lowStockSkuCount(lowStockSkus)
                .build();
    }

    @Override
    public List<RevenueChartResponse> getRevenueChart(String type, Integer days) {
        LocalDateTime now = LocalDateTime.now(ZONE_VN);
        List<RevenueChartResponse> result = new ArrayList<>();

        if ("day".equalsIgnoreCase(type)) {
            int pastDays = (days != null && days > 0) ? days : 7;
            LocalDateTime startDate = now.minusDays(pastDays - 1).with(LocalTime.MIN);
            LocalDateTime endDate = now.with(LocalTime.MAX);

            Map<String, Long> dateMap = new LinkedHashMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (int i = 0; i < pastDays; i++) {
                LocalDate date = startDate.toLocalDate().plusDays(i);
                dateMap.put(date.format(formatter), 0L);
            }

            List<Object[]> rawData = adminOrderService.getRevenueByDay(startDate, endDate);

            for (Object[] row : rawData) {
                String dateLabel = (String) row[0];
                Long revenue = ((Number) row[1]).longValue();
                dateMap.put(dateLabel, revenue);
            }

            for (Map.Entry<String, Long> entry : dateMap.entrySet()) {
                result.add(RevenueChartResponse.builder()
                        .label(entry.getKey())
                        .revenue(entry.getValue())
                        .build());
            }

        } else if ("month".equalsIgnoreCase(type)) {

            LocalDateTime startDate = now.with(TemporalAdjusters.firstDayOfYear()).with(LocalTime.MIN);
            LocalDateTime endDate = now.with(LocalTime.MAX);

            Map<String, Long> monthMap = new LinkedHashMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

            for (int i = 1; i <= 12; i++) {
                LocalDate date = LocalDate.of(now.getYear(), i, 1);
                monthMap.put(date.format(formatter), 0L);
            }

            List<Object[]> rawData = adminOrderService.getRevenueByMonth(startDate, endDate);

            for (Object[] row : rawData) {
                String monthLabel = (String) row[0];
                Long revenue = ((Number) row[1]).longValue();
                monthMap.put(monthLabel, revenue);
            }

            for (Map.Entry<String, Long> entry : monthMap.entrySet()) {
                result.add(RevenueChartResponse.builder()
                        .label(entry.getKey())
                        .revenue(entry.getValue())
                        .build());
            }
        }

        return result;
    }

    @Override
    public Page<ProductSalesResponse> getProductSales(String timeFilter, String sortDirection, int page, int size) {
        LocalDateTime now = LocalDateTime.now(ZONE_VN);
        LocalDateTime startDate = null;
        LocalDateTime endDate = now.with(LocalTime.MAX);

        if ("this_week".equalsIgnoreCase(timeFilter)) {
            int currentDayOfWeek = now.getDayOfWeek().getValue();
            startDate = now.minusDays(currentDayOfWeek - 1).with(LocalTime.MIN);
        } else if ("this_month".equalsIgnoreCase(timeFilter)) {
            startDate = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        } else if ("this_year".equalsIgnoreCase(timeFilter)) {
            startDate = now.with(TemporalAdjusters.firstDayOfYear()).with(LocalTime.MIN);
        } else {
            startDate = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        }

        return productService.getProductSalesAnalytics(startDate, endDate, sortDirection, page, size);
    }
}