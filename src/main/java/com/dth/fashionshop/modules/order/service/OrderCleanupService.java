package com.dth.fashionshop.modules.order.service;

import com.dth.fashionshop.modules.catalog.service.ProductService;
import com.dth.fashionshop.modules.order.entity.Order;
import com.dth.fashionshop.modules.order.entity.OrderDetail;
import com.dth.fashionshop.modules.order.enums.OrderStatus;
import com.dth.fashionshop.modules.order.enums.PaymentStatus;
import com.dth.fashionshop.modules.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCleanupService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Lazy
    @Autowired
    private OrderCleanupService self;

    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredPendingOrders() {
        log.info("[CRON JOB] Quét và dọn dẹp đơn hàng VNPAY hết hạn...");

        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);

        List<Long> expiredOrderIds = orderRepository.findExpiredVnpayOrderIds(thirtyMinutesAgo);

        if (expiredOrderIds.isEmpty()) {
            log.info("Không có đơn hàng rác nào cần dọn dẹp.");
            return;
        }

        int successCount = 0;
        for (Long orderId : expiredOrderIds) {
            try {
                self.processSingleExpiredOrder(orderId);
                successCount++;
            } catch (Exception e) {
                log.error("Lỗi khi dọn dẹp đơn hàng ID {}: {}", orderId, e.getMessage());
            }
        }

        log.info("[CRON JOB] Hoàn tất. Đã xử lý thành công {}/{} đơn hàng rác.", successCount, expiredOrderIds.size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleExpiredOrder(Long orderId) {

        Order order = orderRepository.findByIdWithLockForAdmin(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getOrderStatus() != OrderStatus.PENDING || order.getPaymentStatus() != PaymentStatus.UNPAID) {
            log.warn("Đơn hàng {} đã thay đổi trạng thái (Status: {}, Payment: {}). Bỏ qua hủy đơn.",
                    order.getOrderCode(), order.getOrderStatus(), order.getPaymentStatus());
            return;
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        if (order.getOrderDetails() != null) {
            for (OrderDetail item : order.getOrderDetails()) {
                Long variantId = item.getVariant().getId();
                Integer quantity = item.getQuantity();

                productService.increaseStockWithLock(variantId, quantity);
            }
        }

        log.info("Đã hủy tự động và hoàn tồn kho cho đơn hàng: {}", order.getOrderCode());
    }
}