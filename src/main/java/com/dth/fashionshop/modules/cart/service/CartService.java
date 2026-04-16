package com.dth.fashionshop.modules.cart.service;

import com.dth.fashionshop.modules.cart.dto.request.AddToCartRequest;
import com.dth.fashionshop.modules.cart.dto.request.UpdateCartItemRequest;
import com.dth.fashionshop.modules.cart.dto.response.CartResponse;
import com.dth.fashionshop.modules.cart.dto.response.MiniCartResponse;

public interface CartService {

    void addToCart(AddToCartRequest request);

    CartResponse getMyCart();

    void updateItemQuantity(Long itemId, UpdateCartItemRequest request);

    MiniCartResponse getMiniCart();

    void removeCartItem(Long itemId);
}