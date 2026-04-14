package com.dth.fashionshop.modules.cart.service.impl;

import com.dth.fashionshop.modules.cart.dto.request.AddToCartRequest;
import com.dth.fashionshop.modules.cart.entity.Cart;
import com.dth.fashionshop.modules.cart.entity.CartItem;
import com.dth.fashionshop.modules.cart.repository.CartRepository;
import com.dth.fashionshop.modules.cart.service.CartService;
import com.dth.fashionshop.modules.catalog.entity.ProductVariant;
import com.dth.fashionshop.modules.catalog.service.ProductService;
import com.dth.fashionshop.modules.identity.entity.User;
import com.dth.fashionshop.modules.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserService userService;
    private final ProductService productService;

    @Override
    @Transactional
    public void addToCart(AddToCartRequest request) {

        User user = userService.getCurrentAuthenticatedUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> Cart.builder().user(user).build());

        ProductVariant variant = productService.getVariantEntityBySku(request.getSkuCode());

        if (variant.getProduct().getIsDeleted()) {
            throw new RuntimeException("Sản phẩm này đã ngừng kinh doanh!");
        }
        if (!variant.getIsActive()) {
            throw new RuntimeException("Phân loại sản phẩm này hiện đang tạm ẩn hoặc đã ngừng kinh doanh!");
        }

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getVariant().getId().equals(variant.getId()))
                .findFirst();

        int currentQuantityInCart = existingItemOpt.map(CartItem::getQuantity).orElse(0);
        int totalRequestedQuantity = currentQuantityInCart + request.getQuantity();

        if (totalRequestedQuantity > variant.getStockQuantity()) {
            throw new RuntimeException("Số lượng sản phẩm trong kho không đủ (Còn: " + variant.getStockQuantity() + ")");
        }

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(totalRequestedQuantity);

            log.info("User [{}] đã cộng dồn thêm {} sản phẩm SKU [{}] vào giỏ hàng. (Tổng hiện tại: {})",
                    user.getEmail(), request.getQuantity(), variant.getSkuCode(), totalRequestedQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);

            log.info("User [{}] đã thêm mới {} sản phẩm SKU [{}] vào giỏ hàng.",
                    user.getEmail(), request.getQuantity(), variant.getSkuCode());
        }
        cartRepository.save(cart);
    }
}