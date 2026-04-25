package com.dth.fashionshop.modules.order.service.impl;

import com.dth.fashionshop.modules.cart.dto.response.CartItemCheckoutResponse;
import com.dth.fashionshop.modules.cart.service.CartService;
import com.dth.fashionshop.modules.catalog.service.ProductService;
import com.dth.fashionshop.modules.identity.dto.response.AddressResponse;
import com.dth.fashionshop.modules.identity.entity.User;
import com.dth.fashionshop.modules.identity.service.AddressService;
import com.dth.fashionshop.modules.identity.service.UserService;
import com.dth.fashionshop.modules.order.dto.request.CheckoutPreviewRequest;
import com.dth.fashionshop.modules.order.dto.request.CreateOrderRequest;
import com.dth.fashionshop.modules.order.dto.response.*;
import com.dth.fashionshop.modules.order.entity.Order;
import com.dth.fashionshop.modules.order.entity.OrderDetail;
import com.dth.fashionshop.modules.order.enums.OrderStatus;
import com.dth.fashionshop.modules.order.enums.OrderTab;
import com.dth.fashionshop.modules.order.enums.PaymentMethod;
import com.dth.fashionshop.modules.order.enums.PaymentStatus;
import com.dth.fashionshop.modules.order.repository.OrderRepository;
import com.dth.fashionshop.modules.order.service.OrderService;
import com.dth.fashionshop.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final AddressService addressService;
    private final CartService cartService;
    private final ProductService productService;

    @Value("${app.order.default-shipping-fee:30000}")
    private Long defaultShippingFee;

    private OrderResponse mapToOrderResponse(Order order, String message) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .finalAmount(order.getFinalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentMethod(order.getPaymentMethod())
                .message(message)
                .build();
    }

    private OrderListResponse mapToOrderListResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderDetails().stream()
                .limit(2)
                .map(this::mapToOrderItemResponse)
                .toList();

        return OrderListResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .finalAmount(order.getFinalAmount())
                .createdAt(order.getCreatedAt())
                .items(items)
                .totalItems(order.getOrderDetails().size())
                .build();
    }

    private OrderDetailResponse mapToOrderDetailResponse(Order order) {
        List<OrderItemResponse> allItems = order.getOrderDetails().stream()
                .map(this::mapToOrderItemResponse)
                .toList();

        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .shippingAddress(order.getShippingAddress())
                .customerNote(order.getCustomerNote())
                .totalAmount(order.getTotalAmount())
                .shippingFee(order.getShippingFee())
                .finalAmount(order.getFinalAmount())
                .createdAt(order.getCreatedAt())
                .items(allItems)
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

    // Hàm lấy thông tin để hiển thị ra trang checkout
    @Override
    @Transactional(readOnly = true)
    public CheckoutPreviewResponse previewOrder(CheckoutPreviewRequest request) {

        AddressResponse selectedAddress = null;

        if (request.getAddressId() != null) {
            selectedAddress = addressService.getAddressById(request.getAddressId());
        } else {
            List<AddressResponse> myAddresses = addressService.getMyAddresses();
            if (!myAddresses.isEmpty()) {
                selectedAddress = myAddresses.get(0);
            }
        }

        List<CartItemCheckoutResponse> checkoutItems = cartService.getCheckoutItems(request.getCartItemIds());

        if (checkoutItems.isEmpty()) {
            throw new RuntimeException("Không có sản phẩm nào hợp lệ để thanh toán!");
        }

        long totalAmount = 0L;
        for (CartItemCheckoutResponse item : checkoutItems) {
            totalAmount += (item.getUnitPrice() * item.getQuantity());
        }

        long finalAmount = totalAmount + defaultShippingFee;

        return CheckoutPreviewResponse.builder()
                .shippingAddress(selectedAddress)
                .items(checkoutItems)
                .totalAmount(totalAmount)
                .shippingFee(defaultShippingFee)
                .finalAmount(finalAmount)
                .build();
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        User user = userService.getCurrentAuthenticatedUser();

        AddressResponse addressInfo = addressService.getAddressById(request.getAddressId());
        String fullShippingAddress = String.format("%s, %s, %s, %s",
                addressInfo.getStreet(), addressInfo.getWard(),
                addressInfo.getDistrict(), addressInfo.getCity());

        List<CartItemCheckoutResponse> checkoutItems = cartService.getCheckoutItems(request.getCartItemIds());

        if (checkoutItems.isEmpty()) {
            throw new RuntimeException("Đơn hàng không có sản phẩm nào hợp lệ!");
        }

        long totalAmount = 0L;

        Order order = Order.builder()
                .user(user)
                .receiverName(addressInfo.getReceiverName())
                .receiverPhone(addressInfo.getReceiverPhone())
                .shippingAddress(fullShippingAddress)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.UNPAID)
                .orderStatus(OrderStatus.PENDING)
                .shippingFee(defaultShippingFee)
                .discountAmount(0L)
                .customerNote(request.getCustomerNote())
                .build();

        for (CartItemCheckoutResponse item : checkoutItems) {
            productService.decreaseStockWithLock(item.getVariantId(), item.getQuantity());

            long subtotal = item.getUnitPrice() * item.getQuantity();
            totalAmount += subtotal;

            OrderDetail orderDetail = OrderDetail.builder()
                    .order(order)
                    .variant(productService.getVariantEntityBySku(item.getSkuCode()))
                    .price(item.getUnitPrice())
                    .quantity(item.getQuantity())
                    .subtotal(subtotal)
                    .build();

            order.getOrderDetails().add(orderDetail);
        }

        long finalAmountFromDB = totalAmount + defaultShippingFee;

        if (finalAmountFromDB != request.getExpectedTotalAmount()) {
            throw new RuntimeException("Giá của một số sản phẩm hoặc phí vận chuyển đã thay đổi. Vui lòng tải lại trang để cập nhật giá mới nhất!");
        }

        order.setTotalAmount(totalAmount);
        order.setFinalAmount(finalAmountFromDB);

        order.setOrderCode("ORD-" + System.currentTimeMillis() + "-" + user.getId());

        Order savedOrder = orderRepository.save(order);

        cartService.clearCheckedOutItems(request.getCartItemIds());

        log.info("User [{}] đã tạo thành công Đơn hàng ID [{}] với giá trị {} VNĐ", user.getEmail(), savedOrder.getId(), savedOrder.getFinalAmount());

        return mapToOrderResponse(savedOrder, "Đặt hàng thành công!");
    }

    // Hàm lấy danh sách đơn đặt hàng của khách hàng theo tab trạng thái
    @Override
    @Transactional(readOnly = true)
    public Page<OrderListResponse> getMyOrders(OrderTab tab, int page, int size) {
        User user = userService.getCurrentAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Order> orderPage;

        if (tab == OrderTab.WAITING_PAYMENT) {
            orderPage = orderRepository.findByUserAndOrderStatusAndPaymentStatusAndPaymentMethod(
                    user, OrderStatus.PENDING, PaymentStatus.UNPAID, PaymentMethod.VNPAY, pageable);
        } else if (tab == OrderTab.ALL) {
            orderPage = orderRepository.findByUser(user, pageable);
        } else {
            // Ép kiểu ngược từ OrderTab sang OrderStatus vì chúng có tên giống hệt nhau
            OrderStatus status = OrderStatus.valueOf(tab.name());
            orderPage = orderRepository.findByUserAndOrderStatus(user, status, pageable);
        }

        return orderPage.map(this::mapToOrderListResponse);
    }

    // Hàm load lại thông tin đặt hàng thành công
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderSuccessInfo(String orderCode) {
        User user = userService.getCurrentAuthenticatedUser();
        Order order = orderRepository.findByOrderCodeAndUser(orderCode, user)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));

        return mapToOrderResponse(order, "Truy xuất thông tin thành công");
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(String orderCode) {
        User user = userService.getCurrentAuthenticatedUser();
        Order order = orderRepository.findByOrderCodeAndUser(orderCode, user)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));

        return mapToOrderDetailResponse(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        User user = userService.getCurrentAuthenticatedUser();

        Order order = orderRepository.findByIdAndUserWithLock(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));

        if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.PROCESSING) {
            throw new RuntimeException("Đơn hàng đã được xử lý hoặc vận chuyển, không thể hủy!");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Đơn hàng đã thanh toán. Vui lòng liên hệ cửa hàng để được hỗ trợ hủy và hoàn tiền.");
        }

        for (OrderDetail detail : order.getOrderDetails()) {
            productService.increaseStockWithLock(detail.getVariant().getId(), detail.getQuantity());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("Người dùng {} đã hủy đơn hàng mã: {}", user.getEmail(), order.getOrderCode());
    }
}