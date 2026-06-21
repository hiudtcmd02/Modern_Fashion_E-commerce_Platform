package com.dth.fashionshop.modules.order.service.impl;

import com.dth.fashionshop.config.VNPayConfig;
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
import com.dth.fashionshop.modules.order.entity.PaymentTransaction;
import com.dth.fashionshop.modules.order.enums.*;
import com.dth.fashionshop.modules.order.repository.OrderRepository;
import com.dth.fashionshop.modules.order.repository.PaymentTransactionRepository;
import com.dth.fashionshop.modules.order.service.OrderService;
import com.dth.fashionshop.shared.email.EmailService;
import com.dth.fashionshop.shared.exception.ResourceNotFoundException;
import com.dth.fashionshop.shared.utils.VNPayUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final AddressService addressService;
    private final CartService cartService;
    private final ProductService productService;

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final VNPayConfig vnPayConfig;
    private final EmailService mailService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.order.default-shipping-fee:30000}")
    private Long defaultShippingFee;

    @Value("${spring.mail.username}")
    private String adminEmail;

    private OrderResponse mapToOrderResponse(Order order, String message, String paymentUrl) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .finalAmount(order.getFinalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentMethod(order.getPaymentMethod())
                .message(message)
                .paymentUrl(paymentUrl)
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

    private PaymentResultResponse buildReturnResponse(Order order, TransactionStatus status) {
        boolean isSuccess = status.equals(TransactionStatus.SUCCESS);
        return PaymentResultResponse.builder()
                .isSuccess(isSuccess)
                .message(isSuccess ? "Thanh toán thành công" : "Thanh toán thất bại")
                .orderCode(order.getOrderCode())
                .amount(order.getFinalAmount())
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
    public OrderResponse createOrder(CreateOrderRequest request, HttpServletRequest httpServletRequest) {
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

        String paymentUrl = null;
        if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .order(savedOrder)
                    .amount(finalAmountFromDB)
                    .provider(PaymentProvider.VNPAY)
                    .status(TransactionStatus.PENDING)
                    .build();
            transaction = paymentTransactionRepository.save(transaction);

            paymentUrl = generateVnPayUrl(savedOrder, transaction.getId(), finalAmountFromDB, httpServletRequest);
        }

        return mapToOrderResponse(savedOrder, "Đặt hàng thành công!", paymentUrl);
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

    // Hàm load lại thông tin đặt hàng thành công (đơn COD)
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderSuccessInfo(String orderCode) {
        User user = userService.getCurrentAuthenticatedUser();
        Order order = orderRepository.findByOrderCodeAndUser(orderCode, user)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));

        return mapToOrderResponse(order, "Truy xuất thông tin thành công", null);
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

    @Override
    @Transactional
    public PaymentResultResponse processReturnUrl(HttpServletRequest request) {
        Map<String, String> fields = getParamsFromRequest(request);
        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        String signValue = VNPayUtils.hashAllFields(fields, vnPayConfig.getVnpHashSecret());
        if (!signValue.equals(vnp_SecureHash)) {
            throw new RuntimeException("Lỗi bảo mật: Sai chữ ký xác thực!");
        }

        String vnp_TxnRef = request.getParameter("vnp_TxnRef").replace("TXN_", "");
        Long txnId = Long.parseLong(vnp_TxnRef);

        PaymentTransaction transaction = paymentTransactionRepository.findByIdForUpdate(txnId)
                .orElseThrow(() -> new RuntimeException("Lỗi dữ liệu: Không tìm thấy giao dịch TXN_" + txnId));

        long vnp_Amount = Long.parseLong(request.getParameter("vnp_Amount"));
        long dbAmount = transaction.getAmount() * 100;

        if (vnp_Amount != dbAmount) {
            log.error("Lỗi Return URL: Sai lệch số tiền giao dịch TXN_{}", txnId);
            throw new RuntimeException("Lỗi dữ liệu: Số tiền giao dịch không khớp với hệ thống!");
        }

        Order order = transaction.getOrder();
        String responseCode = request.getParameter("vnp_ResponseCode");

        boolean isSalvaged = false;
        if (transaction.getStatus().equals(TransactionStatus.FAILED) && "00".equals(responseCode)) {
            log.info("Return URL: Phát hiện hành vi vớt vát giao dịch TXN_{} thành SUCCESS", txnId);
            isSalvaged = true;
        }

        if (!isSalvaged && !transaction.getStatus().equals(TransactionStatus.PENDING)) {
            return buildReturnResponse(order, transaction.getStatus());
        }

        updateTransactionDetails(transaction, request, fields);

        if ("00".equals(responseCode)) {
            transaction.setStatus(TransactionStatus.SUCCESS);

            if (order.getPaymentStatus() == PaymentStatus.PAID) {
                log.warn("Return URL: Đơn hàng {} đã được PAID trước đó. Chỉ cập nhật Transaction TXN_{} thành SUCCESS để đối soát.", order.getOrderCode(), txnId);
                paymentTransactionRepository.save(transaction);

                return buildReturnResponse(order, TransactionStatus.SUCCESS);
            }

            if (order.getOrderStatus() == OrderStatus.CANCELLED) {
                log.error("CẢNH BÁO: Đơn hàng {} đã bị hủy (Timeout) nhưng VNPAY lại báo thanh toán thành công!", order.getOrderCode());

                String warningSubject = "[CHÚ Ý] ĐƠN HÀNG BỊ HỦY NHƯNG TIỀN ĐÃ VỀ";
                String warningBody = String.format("Hệ thống ghi nhận giao dịch VNPAY thành công cho một đơn hàng đã bị hủy (Do khách thanh toán đúng lúc Cronjob dọn dẹp chạy).\n" +
                                "- Mã Đơn hàng: %s\n" +
                                "- Mã Giao dịch VNPAY: TXN_%d\n" +
                                "- Số tiền: %d VND\n\n" +
                                "YÊU CẦU XỬ LÝ:\n" +
                                "1. CSKH liên hệ xin lỗi khách hàng, xin thông tin để hoàn tiền và hướng dẫn đặt lại đơn mới.\n" +
                                "2. Kế toán tiến hành hoàn tiền thủ công (Refund) cho giao dịch này và yêu cầu đội Dev chuyển trạng thái thanh toán của đơn hàng thành ĐÃ HOÀN TIỀN.",
                        order.getOrderCode(), txnId, transaction.getAmount());
                mailService.sendEmail(adminEmail, warningSubject, warningBody);
            }

            order.setPaymentStatus(PaymentStatus.PAID);
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
        }

        paymentTransactionRepository.save(transaction);
        orderRepository.save(order);

        return buildReturnResponse(order, transaction.getStatus());
    }

    @Override
    @Transactional
    public IpnResponse processIpn(HttpServletRequest request) {
        try {
            Map<String, String> fields = getParamsFromRequest(request);
            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            fields.remove("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");

            String signValue = VNPayUtils.hashAllFields(fields, vnPayConfig.getVnpHashSecret());
            if (!signValue.equals(vnp_SecureHash)) {
                return new IpnResponse("97", "Invalid Checksum");
            }

            String vnp_TxnRef = request.getParameter("vnp_TxnRef").replace("TXN_", "");
            Long txnId = Long.parseLong(vnp_TxnRef);

            PaymentTransaction transaction = paymentTransactionRepository.findByIdForUpdate(txnId).orElse(null);

            if (transaction == null) {
                String warningSubject = "CẢNH BÁO: VNPAY HOÀN THÀNH GIAO DỊCH NHƯNG KHÔNG CÓ ĐƠN HÀNG!";
                String warningBody = String.format("[CẢNH BÁO] VNPay báo hoàn thành giao dịch nhưng Backend không tìm thấy thông tin giao dịch trong DB.\n" +
                                "- Mã GD (TxnRef): TXN_%d\n" +
                                "- Số tiền VNPay báo: %s VND\n" +
                                "- Ngân hàng: %s\n" +
                                "Yêu cầu Kế toán và Dev kiểm tra và xử lý gấp trên Merchant Portal nếu giao dịch đã thành công!",
                        txnId,
                        Long.parseLong(request.getParameter("vnp_Amount")) / 100,
                        request.getParameter("vnp_BankCode"));

                mailService.sendEmail(adminEmail, warningSubject, warningBody);
                log.error("IPN ERROR: Không tìm thấy giao dịch TXN_{} trong khi VNPay báo hoàn thành giao dịch!", txnId);

                return new IpnResponse("01", "Order not found");
            }

            long vnp_Amount = Long.parseLong(request.getParameter("vnp_Amount"));
            long dbAmount = transaction.getAmount() * 100;
            if (vnp_Amount != dbAmount) {
                String warningSubject = "CẢNH BÁO: SAI LỆCH SỐ TIỀN VNPAY";
                String warningBody = String.format("Giao dịch TXN_%d có dấu hiệu sai lệch số tiền!\n" +
                                "Tiền VNPay trả về: %d VND,\n " +
                                "Tiền Database: %d VND",
                        txnId, vnp_Amount / 100, transaction.getAmount());
                mailService.sendEmail(adminEmail, warningSubject, warningBody);
                return new IpnResponse("04", "Invalid amount");
            }

            if (!transaction.getStatus().equals(TransactionStatus.PENDING)) {
                String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");

                if (transaction.getStatus().equals(TransactionStatus.FAILED) && "00".equals(vnp_ResponseCode)) {

                    Order order = transaction.getOrder();
                    if (order.getPaymentStatus() == PaymentStatus.PAID) {
                        log.error("CẢNH BÁO: THANH TOÁN KÉP! Đơn hàng {} nhận thêm tiền từ TXN_{}", order.getOrderCode(), txnId);

                        String warningSubject = "[KHẨN CẤP] THANH TOÁN KÉP VNPAY";
                        String warningBody = String.format("Khách hàng đã thanh toán 2 lần cho cùng một đơn hàng!\n" +
                                        "- Mã Đơn hàng: %s\n" +
                                        "- Mã Giao dịch trùng lặp: TXN_%d\n" +
                                        "Yêu cầu Kế toán kiểm tra Merchant Portal và hoàn tiền ngay lập tức!",
                                order.getOrderCode(), txnId);
                        mailService.sendEmail(adminEmail, warningSubject, warningBody);

                        updateTransactionDetails(transaction, request, fields);

                        transaction.setStatus(TransactionStatus.SUCCESS);
                        paymentTransactionRepository.save(transaction);

                        return new IpnResponse("00", "Confirm Success");
                    }

                    log.info("Vớt vát giao dịch TXN_{} thành SUCCESS (Khách hàng thanh toán trên link cũ)", txnId);
                } else {
                    return new IpnResponse("02", "Order already confirmed");
                }
            }

            updateTransactionDetails(transaction, request, fields);

            String responseCode = request.getParameter("vnp_ResponseCode");
            Order order = transaction.getOrder();

            if ("00".equals(responseCode)) {
                transaction.setStatus(TransactionStatus.SUCCESS);

                if (order.getOrderStatus() == OrderStatus.CANCELLED) {
                    log.error("CẢNH BÁO: Đơn hàng {} đã bị hủy (Timeout) nhưng VNPAY lại báo thanh toán thành công!", order.getOrderCode());

                    String warningSubject = "[CHÚ Ý] ĐƠN HÀNG BỊ HỦY NHƯNG TIỀN ĐÃ VỀ";
                    String warningBody = String.format("Hệ thống ghi nhận giao dịch VNPAY thành công cho một đơn hàng đã bị hủy (Do khách thanh toán đúng lúc Cronjob dọn dẹp chạy).\n" +
                                    "- Mã Đơn hàng: %s\n" +
                                    "- Mã Giao dịch VNPAY: TXN_%d\n" +
                                    "- Số tiền: %d VND\n\n" +
                                    "YÊU CẦU XỬ LÝ:\n" +
                                    "1. CSKH liên hệ xin lỗi khách hàng, xin thông tin để hoàn tiền và hướng dẫn đặt lại đơn mới.\n" +
                                    "2. Kế toán tiến hành hoàn tiền thủ công (Refund) cho giao dịch này và yêu cầu đội Dev chuyển trạng thái thanh toán của đơn hàng thành ĐÃ HOÀN TIỀN.",
                            order.getOrderCode(), txnId, transaction.getAmount());
                    mailService.sendEmail(adminEmail, warningSubject, warningBody);
                }

                order.setPaymentStatus(PaymentStatus.PAID);
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
            }

            paymentTransactionRepository.save(transaction);
            orderRepository.save(order);

            return new IpnResponse("00", "Confirm Success");

        } catch (Exception e) {
            log.error("Lỗi xử lý IPN: ", e);
            return new IpnResponse("99", "Unknown error");
        }
    }

    @Override
    @Transactional
    public String retryPayment(String orderCode, HttpServletRequest request) {
        User user = userService.getCurrentAuthenticatedUser();
        Order order = orderRepository.findByOrderCodeAndUser(orderCode, user)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng của bạn!"));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Đơn hàng đã được thanh toán thành công trước đó!");
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Đơn hàng đã bị hủy, không thể thực hiện thanh toán!");
        }

        PaymentTransaction newTransaction = PaymentTransaction.builder()
                .order(order)
                .amount(order.getFinalAmount())
                .provider(PaymentProvider.VNPAY)
                .status(TransactionStatus.PENDING)
                .build();
        newTransaction = paymentTransactionRepository.save(newTransaction);

        return generateVnPayUrl(order, newTransaction.getId(), order.getFinalAmount(), request);
    }

    // Hàm hỗ trợ lưu thông tin giao dịch
    private void updateTransactionDetails(PaymentTransaction transaction, HttpServletRequest request, Map<String, String> fields) {
        transaction.setTransactionNo(request.getParameter("vnp_TransactionNo"));
        transaction.setBankCode(request.getParameter("vnp_BankCode"));
        transaction.setVnpResponseCode(request.getParameter("vnp_ResponseCode"));

        try {
            transaction.setTransactionPayload(objectMapper.writeValueAsString(fields));
        } catch (Exception e) {
            log.error("Không thể parse transaction payload sang JSON", e);
        }

        String payDateStr = request.getParameter("vnp_PayDate");
        if (payDateStr != null && payDateStr.length() == 14) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            transaction.setPayDate(LocalDateTime.parse(payDateStr, formatter));
        }
    }

    // Hàm lấy ra các params từ HttpServletRequest
    private Map<String, String> getParamsFromRequest(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
            }
        }
        return fields;
    }

    // Hàm sinh URL VNPay
    private String generateVnPayUrl(Order order, Long transactionId, Long amount, HttpServletRequest request) {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnPayConfig.getVnpVersion());
        vnp_Params.put("vnp_Command", vnPayConfig.getVnpCommand());
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", "TXN_" + transactionId);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderCode());
        vnp_Params.put("vnp_OrderType", "fashion");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
        vnp_Params.put("vnp_IpAddr", VNPayUtils.getIpAddress(request));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        vnp_Params.put("vnp_CreateDate", now.format(formatter));
        vnp_Params.put("vnp_ExpireDate", now.plusMinutes(15).format(formatter));

        StringBuilder query = new StringBuilder();
        vnp_Params.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII));
                    query.append("=");
                    query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
                    query.append("&");
                });
        String queryUrl = query.substring(0, query.length() - 1);

        String vnp_SecureHash = VNPayUtils.hashAllFields(vnp_Params, vnPayConfig.getVnpHashSecret());

        return vnPayConfig.getVnpPayUrl() + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;
    }
}