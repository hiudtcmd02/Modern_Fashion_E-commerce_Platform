package com.dth.fashionshop.modules.cart.controller;

import com.dth.fashionshop.modules.cart.dto.request.AddToCartRequest;
import com.dth.fashionshop.modules.cart.dto.request.UpdateCartItemRequest;
import com.dth.fashionshop.modules.cart.dto.response.CartResponse;
import com.dth.fashionshop.modules.cart.dto.response.MiniCartResponse;
import com.dth.fashionshop.modules.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/items")
    public ResponseEntity<?> addToCart(@Valid @RequestBody AddToCartRequest request) {

        cartService.addToCart(request);

        return ResponseEntity.ok(Map.of("message", "Đã thêm sản phẩm vào giỏ hàng thành công"));
    }

    // Xem chi tiết giỏ hàng
    @GetMapping
    public ResponseEntity<CartResponse> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    // Cập nhật số lượng của một sản phẩm trong giỏ hàng
    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateItemQuantity(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        cartService.updateItemQuantity(itemId, request);

        return ResponseEntity.ok(Map.of("message", "Cập nhật số lượng thành công"));
    }

    // Xem giỏ hàng thu nhỏ trên Header
    @GetMapping("/mini")
    public ResponseEntity<MiniCartResponse> getMiniCart() {
        return ResponseEntity.ok(cartService.getMiniCart());
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeCartItem(@PathVariable Long itemId) {

        cartService.removeCartItem(itemId);

        return ResponseEntity.ok(Map.of("message", "Đã xóa sản phẩm khỏi giỏ hàng"));
    }
}