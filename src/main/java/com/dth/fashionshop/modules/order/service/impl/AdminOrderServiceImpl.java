package com.dth.fashionshop.modules.order.service.impl;

import com.dth.fashionshop.modules.catalog.service.ProductService;
import com.dth.fashionshop.modules.order.dto.request.UpdateOrderStatusRequest;
import com.dth.fashionshop.modules.order.dto.response.AdminOrderDetailResponse;
import com.dth.fashionshop.modules.order.dto.response.AdminOrderListResponse;
import com.dth.fashionshop.modules.order.dto.response.OrderItemResponse;
import com.dth.fashionshop.modules.order.entity.Order;
import com.dth.fashionshop.modules.order.entity.OrderDetail;
import com.dth.fashionshop.modules.order.enums.OrderStatus;
import com.dth.fashionshop.modules.order.enums.OrderTab;
import com.dth.fashionshop.modules.order.enums.PaymentMethod;
import com.dth.fashionshop.modules.order.enums.PaymentStatus;
import com.dth.fashionshop.modules.order.repository.OrderRepository;
import com.dth.fashionshop.modules.order.service.AdminOrderService;
import com.dth.fashionshop.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    private AdminOrderListResponse mapToAdminListResponse(Order order) {
        return AdminOrderListResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .customerName(order.getUser().getFullName())
                .customerPhone(order.getUser().getPhoneNumber())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .finalAmount(order.getFinalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private AdminOrderDetailResponse mapToAdminDetailResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderDetails().stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());

        return AdminOrderDetailResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .customerName(order.getUser().getFullName())
                .customerEmail(order.getUser().getEmail())
                .customerPhone(order.getUser().getPhoneNumber())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .shippingAddress(order.getShippingAddress())
                .customerNote(order.getCustomerNote())
                .internalNote(order.getInternalNote())
                .totalAmount(order.getTotalAmount())
                .shippingFee(order.getShippingFee())
                .finalAmount(order.getFinalAmount())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private OrderItemResponse mapToOrderItemResponse(OrderDetail detail) {
        return OrderItemResponse.builder()
                .variantId(detail.getVariant().getId())
                .skuCode(detail.getVariant().getSkuCode())
                .productName(detail.getVariant().getProduct().getName())
                .variantName(detail.getVariant().getVariantName())
                .thumbnailUrl(detail.getVariant().getProduct().getThumbnailUrl())
                .unitPrice(detail.getPrice())
                .quantity(detail.getQuantity())
                .subtotal(detail.getSubtotal())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminOrderListResponse> getAllOrdersForAdmin(String keyword, OrderTab tab, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage;

        if (tab == OrderTab.WAITING_PAYMENT) {
            orderPage = orderRepository.searchAdminWaitingPaymentOrders(keyword, pageable);
        } else if (tab == OrderTab.ALL || tab == null) {
            orderPage = orderRepository.searchAdminOrders(keyword, null, pageable);
        } else {
            OrderStatus status = OrderStatus.valueOf(tab.name());
            orderPage = orderRepository.searchAdminOrders(keyword, status, pageable);
        }

        return orderPage.map(this::mapToAdminListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOrderDetailResponse getOrderDetailForAdmin(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));
        return mapToAdminDetailResponse(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByIdWithLockForAdmin(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));

        OrderStatus oldOrderStatus = order.getOrderStatus();
        PaymentStatus oldPaymentStatus = order.getPaymentStatus();
        OrderStatus newOrderStatus = request.getOrderStatus();
        PaymentStatus newPaymentStatus = request.getPaymentStatus();

        if (oldOrderStatus == OrderStatus.CANCELLED || oldOrderStatus == OrderStatus.RETURNED) {
            if (oldOrderStatus != newOrderStatus || oldPaymentStatus != newPaymentStatus) {
                throw new RuntimeException("Đơn hàng này đã được ĐÓNG (Đã Hủy/Trả). Chỉ có thể cập nhật ghi chú nội bộ, không thể đổi trạng thái!");
            }

            if (request.getInternalNote() != null) {
                order.setInternalNote(request.getInternalNote());
                orderRepository.save(order);
                log.info("Admin đã cập nhật ghi chú nội bộ cho đơn hàng đã đóng {}", order.getOrderCode());
            }
            return;
        }

        if (!isValidOrderTransition(oldOrderStatus, newOrderStatus)) {
            throw new RuntimeException("Luồng trạng thái đơn hàng không hợp lệ! Không thể chuyển từ " + oldOrderStatus + " sang " + newOrderStatus);
        }

        if (!isValidPaymentTransition(oldPaymentStatus, newPaymentStatus)) {
            throw new RuntimeException("Luồng trạng thái thanh toán không hợp lệ! Không thể chuyển từ " + oldPaymentStatus + " sang " + newPaymentStatus);
        }

        if (order.getPaymentMethod() == PaymentMethod.VNPAY && newPaymentStatus == PaymentStatus.UNPAID) {
            if (newOrderStatus != OrderStatus.PENDING && newOrderStatus != OrderStatus.CANCELLED) {
                throw new RuntimeException("Không thể xử lý hoặc giao hàng khi đơn VNPAY chưa được thanh toán!");
            }
        }

        if (oldPaymentStatus == PaymentStatus.PAID && (newOrderStatus == OrderStatus.CANCELLED || newOrderStatus == OrderStatus.RETURNED)) {
            if (newPaymentStatus != PaymentStatus.REFUNDED) {
                throw new RuntimeException("Đơn hàng đã được thanh toán. Vui lòng xác nhận đã hoàn tiền (REFUNDED) trước khi Hủy/Trả đơn.");
            }
        }

        if (newPaymentStatus == PaymentStatus.REFUNDED) {
            if (newOrderStatus != OrderStatus.CANCELLED && newOrderStatus != OrderStatus.RETURNED) {
                throw new RuntimeException("Lỗi luồng tài chính: Chỉ có thể hoàn tiền (REFUNDED) khi trạng thái đơn hàng là Đã hủy (CANCELLED) hoặc Đã trả hàng (RETURNED)!");
            }
        }

        if (oldPaymentStatus == PaymentStatus.UNPAID && (newOrderStatus == OrderStatus.CANCELLED || newOrderStatus == OrderStatus.RETURNED)) {
            if (newPaymentStatus != PaymentStatus.UNPAID) {
                throw new RuntimeException("Lỗi luồng tài chính: Đơn hàng chưa được thanh toán. Vui lòng giữ nguyên trạng thái thanh toán là Chưa thanh toán (UNPAID) khi Hủy/Trả đơn!");
            }
        }

        if (newOrderStatus == OrderStatus.COMPLETED && order.getPaymentMethod() == PaymentMethod.COD && newPaymentStatus == PaymentStatus.UNPAID) {
            newPaymentStatus = PaymentStatus.PAID;
            log.info("Auto-Trigger: Tự động chuyển trạng thái thanh toán thành PAID cho đơn COD COMPLETED {}", order.getOrderCode());
        }

        if (newOrderStatus == OrderStatus.CANCELLED || newOrderStatus == OrderStatus.RETURNED) {
            for (OrderDetail detail : order.getOrderDetails()) {
                productService.increaseStockWithLock(detail.getVariant().getId(), detail.getQuantity());
            }
            log.info("Đã hoàn kho thành công cho đơn hàng bị Hủy/Trả: {}", order.getOrderCode());
        }

        order.setOrderStatus(newOrderStatus);
        order.setPaymentStatus(newPaymentStatus);

        if (request.getInternalNote() != null) {
            order.setInternalNote(request.getInternalNote());
        }

        orderRepository.save(order);
        log.info("Admin đã cập nhật thành công đơn hàng {}: {} - {}", order.getOrderCode(), newOrderStatus, newPaymentStatus);
    }

    private boolean isValidOrderTransition(OrderStatus oldStatus, OrderStatus newStatus) {
        if (oldStatus == newStatus) return true;

        switch (oldStatus) {
            case PENDING:
                return newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELLED;
            case PROCESSING:
                return newStatus == OrderStatus.SHIPPING || newStatus == OrderStatus.CANCELLED;
            case SHIPPING:
                return newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.RETURNED || newStatus == OrderStatus.CANCELLED;
            case COMPLETED:
                return newStatus == OrderStatus.RETURNED;
            default:
                return false;
        }
    }

    private boolean isValidPaymentTransition(PaymentStatus oldStatus, PaymentStatus newStatus) {
        if (oldStatus == newStatus) return true;

        switch (oldStatus) {
            case UNPAID:
                return newStatus == PaymentStatus.PAID;
            case PAID:
                return newStatus == PaymentStatus.REFUNDED;
            default:
                return false;
        }
    }
}