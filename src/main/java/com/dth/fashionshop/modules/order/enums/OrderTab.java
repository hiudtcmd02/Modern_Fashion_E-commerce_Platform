package com.dth.fashionshop.modules.order.enums;

public enum OrderTab {
    ALL,                // Tất cả
    WAITING_PAYMENT,    // Đang chờ thanh toán (VNPay + PENDING + UNPAID)
    PENDING,            // Đang chờ xử lý
    PROCESSING,         // Đang chuẩn bị đơn hàng
    SHIPPING,           // Đang giao
    COMPLETED,          // Hoàn thành
    CANCELLED,          // Đã hủy
    RETURNED            // Đã trả hàng
}