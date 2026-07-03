package com.dth.fashionshop.modules.order.service;

import com.dth.fashionshop.modules.order.dto.request.UpdateOrderStatusRequest;
import com.dth.fashionshop.modules.order.dto.response.AdminOrderDetailResponse;
import com.dth.fashionshop.modules.order.dto.response.AdminOrderListResponse;
import com.dth.fashionshop.modules.order.enums.OrderTab;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminOrderService {

    Page<AdminOrderListResponse> getAllOrdersForAdmin(String keyword, OrderTab tab, int page, int size);

    AdminOrderDetailResponse getOrderDetailForAdmin(Long id);

    void updateOrderStatus(Long id, UpdateOrderStatusRequest request);

    Long calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate);

    Long countCompletedOrders(LocalDateTime startDate, LocalDateTime endDate);

    List<Object[]> getRevenueByDay(LocalDateTime startDate, LocalDateTime endDate);

    List<Object[]> getRevenueByMonth(LocalDateTime startDate, LocalDateTime endDate);
}