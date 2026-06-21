package com.dth.fashionshop.modules.order.service;

import com.dth.fashionshop.modules.order.dto.request.CheckoutPreviewRequest;
import com.dth.fashionshop.modules.order.dto.request.CreateOrderRequest;
import com.dth.fashionshop.modules.order.dto.response.*;
import com.dth.fashionshop.modules.order.enums.OrderTab;
import org.springframework.data.domain.Page;
import jakarta.servlet.http.HttpServletRequest;

public interface OrderService {

    CheckoutPreviewResponse previewOrder(CheckoutPreviewRequest request);

    OrderResponse createOrder(CreateOrderRequest request, HttpServletRequest httpServletRequest);

    Page<OrderListResponse> getMyOrders(OrderTab tab, int page, int size);

    OrderResponse getOrderSuccessInfo(String orderCode);

    OrderDetailResponse getOrderDetail(String orderCode);

    void cancelOrder(Long orderId);

    PaymentResultResponse processReturnUrl(HttpServletRequest request);

    IpnResponse processIpn(HttpServletRequest request);

    String retryPayment(String orderCode, HttpServletRequest request);
}