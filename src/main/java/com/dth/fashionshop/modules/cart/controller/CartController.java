package com.dth.fashionshop.modules.cart.controller;

import com.dth.fashionshop.modules.cart.dto.request.AddToCartRequest;
import com.dth.fashionshop.modules.cart.dto.request.UpdateCartItemRequest;
import com.dth.fashionshop.modules.cart.dto.response.CartResponse;
import com.dth.fashionshop.modules.cart.dto.response.MiniCartResponse;
import com.dth.fashionshop.modules.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Khách hàng quản lý giỏ hàng cá nhân", description = "Các API dành cho Khách hàng thực hiện các tác vụ liên quan đến giỏ hàng cá nhân")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Thêm sản phẩm vào giỏ hàng")
    @PostMapping("/items")
    public ResponseEntity<?> addToCart(@Valid @RequestBody AddToCartRequest request) {

        cartService.addToCart(request);

        return ResponseEntity.ok(Map.of("message", "Đã thêm sản phẩm vào giỏ hàng thành công"));
    }

    @Operation(summary = "Xem chi tiết giỏ hàng")
    @GetMapping
    public ResponseEntity<CartResponse> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    @Operation(summary = "Cập nhật số lượng của một sản phẩm trong giỏ hàng")
    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateItemQuantity(
            @Parameter(description = "ID bên trong giỏ hàng của sản phẩm")
            @PathVariable Long itemId,

            @Valid @RequestBody UpdateCartItemRequest request) {

        cartService.updateItemQuantity(itemId, request);

        return ResponseEntity.ok(Map.of("message", "Cập nhật số lượng thành công"));
    }

    @Operation(summary = "Lấy thông tin giỏ hàng thu nhỏ (mini cart) nằm trên thanh Header")
    @GetMapping("/mini")
    public ResponseEntity<MiniCartResponse> getMiniCart() {
        return ResponseEntity.ok(cartService.getMiniCart());
    }

    @Operation(summary = "Xóa sản phẩm khỏi giỏ hàng")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeCartItem(
            @Parameter(description = "ID bên trong giỏ hàng của sản phẩm")
            @PathVariable Long itemId)
    {

        cartService.removeCartItem(itemId);

        return ResponseEntity.ok(Map.of("message", "Đã xóa sản phẩm khỏi giỏ hàng"));
    }
}