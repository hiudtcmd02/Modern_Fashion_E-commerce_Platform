package com.dth.fashionshop.modules.statistics.controller;

import com.dth.fashionshop.modules.statistics.dto.response.KpiResponse;
import com.dth.fashionshop.modules.statistics.dto.response.ProductSalesResponse;
import com.dth.fashionshop.modules.statistics.dto.response.RevenueChartResponse;
import com.dth.fashionshop.modules.statistics.service.StatisticsService;
import com.dth.fashionshop.shared.utils.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin thống kê doanh thu và sản phẩm (Dashboard)", description = "Các API dành cho Admin xem báo cáo thống kê trên trang Dashboard")
public class AdminStatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "Lấy các chỉ số tổng quan (KPIs)",
            description = "Trả về dữ liệu cho 4 thẻ KPI trên cùng: Doanh thu tháng này, Tổng đơn hoàn thành, Khách hàng mới, Số lượng phân loại sản phẩm sắp hoặc đã hết hàng.")
    @GetMapping("/kpis")
    public ResponseEntity<KpiResponse> getDashboardKpis() {
        return ResponseEntity.ok(statisticsService.getDashboardKpis());
    }

    @Operation(summary = "Lấy dữ liệu biểu đồ doanh thu",
            description = "Trả về mảng dữ liệu (Label và Revenue) để Frontend vẽ biểu đồ doanh thu.")
    @GetMapping("/revenue")
    public ResponseEntity<List<RevenueChartResponse>> getRevenueChart(
            @Parameter(description = "Loại mốc thời gian hiển thị. Các giá trị hợp lệ bắt buộc: 'day' (Hiển thị theo từng ngày) hoặc 'month' (Hiển thị theo từng tháng trong năm).",
                    example = "day")
            @RequestParam(defaultValue = "day") String type,

            @Parameter(description = "Số ngày lùi về trước để vẽ biểu đồ (Chỉ có tác dụng khi type = 'day'). Khi chọn type = 'day', mà tham số này bỏ trống sẽ lấy mặc định là 7 ngày.",
                    example = "7")
            @RequestParam(required = false) Integer days
    ) {
        return ResponseEntity.ok(statisticsService.getRevenueChart(type, days));
    }

    @Operation(summary = "Lấy thống kê sản phẩm bán ra",
            description = "Hỗ trợ lọc sản phẩm theo thời gian, sắp xếp theo số lượng bán (bán chạy nhất/bán chậm nhất) và phân trang.")
    @GetMapping("/products")
    public ResponseEntity<Page<ProductSalesResponse>> getProductSales(
            @Parameter(description = "Bộ lọc thời gian. Các giá trị hợp lệ bắt buộc: 'this_week' (Tuần này), 'this_month' (Tháng này), 'this_year' (Năm nay).",
                    example = "this_month")
            @RequestParam(defaultValue = "this_month") String time,

            @Parameter(description = "Sắp xếp theo số lượng bán. Các giá trị hợp lệ bắt buộc: 'desc' (Bán chạy nhất - giảm dần), 'asc' (Bán chậm nhất - tăng dần).",
                    example = "desc")
            @RequestParam(defaultValue = "desc") String sort,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        int pageNumber = PaginationUtils.correctPageNo(page);

        return ResponseEntity.ok(statisticsService.getProductSales(time, sort, pageNumber, size));
    }
}