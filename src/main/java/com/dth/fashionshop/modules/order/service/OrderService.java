package com.dth.fashionshop.modules.order.service;

import com.dth.fashionshop.modules.order.dto.request.CheckoutPreviewRequest;
import com.dth.fashionshop.modules.order.dto.request.CreateOrderRequest;
import com.dth.fashionshop.modules.order.dto.response.CheckoutPreviewResponse;
import com.dth.fashionshop.modules.order.dto.response.OrderDetailResponse;
import com.dth.fashionshop.modules.order.dto.response.OrderListResponse;
import com.dth.fashionshop.modules.order.dto.response.OrderResponse;
import com.dth.fashionshop.modules.order.enums.OrderTab;
import org.springframework.data.domain.Page;

public interface OrderService {

    CheckoutPreviewResponse previewOrder(CheckoutPreviewRequest request);

    OrderResponse createOrder(CreateOrderRequest request);

    Page<OrderListResponse> getMyOrders(OrderTab tab, int page, int size);

    OrderResponse getOrderSuccessInfo(String orderCode);

    OrderDetailResponse getOrderDetail(String orderCode);

    void cancelOrder(Long orderId);
}