package com.dth.fashionshop.modules.cart.service;

import com.dth.fashionshop.modules.cart.dto.request.AddToCartRequest;

public interface CartService {

    void addToCart(AddToCartRequest request);

}