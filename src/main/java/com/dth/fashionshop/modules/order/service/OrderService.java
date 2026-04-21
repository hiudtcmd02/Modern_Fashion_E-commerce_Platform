package com.dth.fashionshop.modules.order.service;

import com.dth.fashionshop.modules.order.dto.request.CheckoutPreviewRequest;
import com.dth.fashionshop.modules.order.dto.request.CreateOrderRequest;
import com.dth.fashionshop.modules.order.dto.response.CheckoutPreviewResponse;
import com.dth.fashionshop.modules.order.dto.response.OrderResponse;

public interface OrderService {

    CheckoutPreviewResponse previewOrder(CheckoutPreviewRequest request);

    OrderResponse createOrder(CreateOrderRequest request);
}