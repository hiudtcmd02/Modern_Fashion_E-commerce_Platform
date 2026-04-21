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
import com.dth.fashionshop.modules.order.dto.response.CheckoutPreviewResponse;
import com.dth.fashionshop.modules.order.dto.response.OrderResponse;
import com.dth.fashionshop.modules.order.entity.Order;
import com.dth.fashionshop.modules.order.entity.OrderDetail;
import com.dth.fashionshop.modules.order.enums.OrderStatus;
import com.dth.fashionshop.modules.order.enums.PaymentStatus;
import com.dth.fashionshop.modules.order.repository.OrderRepository;
import com.dth.fashionshop.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

        return OrderResponse.builder()
                .id(savedOrder.getId())
                .orderCode(savedOrder.getOrderCode())
                .finalAmount(savedOrder.getFinalAmount())
                .orderStatus(savedOrder.getOrderStatus())
                .paymentMethod(savedOrder.getPaymentMethod())
                .message("Đặt hàng thành công!")
                .build();
    }
}