package com.dth.fashionshop.modules.cart.service.impl;

import com.dth.fashionshop.modules.cart.dto.request.AddToCartRequest;
import com.dth.fashionshop.modules.cart.dto.request.UpdateCartItemRequest;
import com.dth.fashionshop.modules.cart.dto.response.CartItemResponse;
import com.dth.fashionshop.modules.cart.dto.response.CartResponse;
import com.dth.fashionshop.modules.cart.dto.response.MiniCartResponse;
import com.dth.fashionshop.modules.cart.entity.Cart;
import com.dth.fashionshop.modules.cart.entity.CartItem;
import com.dth.fashionshop.modules.cart.repository.CartItemRepository;
import com.dth.fashionshop.modules.cart.repository.CartRepository;
import com.dth.fashionshop.modules.cart.service.CartService;
import com.dth.fashionshop.modules.catalog.entity.Product;
import com.dth.fashionshop.modules.catalog.entity.ProductVariant;
import com.dth.fashionshop.modules.catalog.service.ProductService;
import com.dth.fashionshop.modules.identity.entity.User;
import com.dth.fashionshop.modules.identity.service.UserService;
import com.dth.fashionshop.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
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

    @Override
    @Transactional(readOnly = true)
    public CartResponse getMyCart() {
        User user = userService.getCurrentAuthenticatedUser();
        Cart cart = cartRepository.findByUser(user).orElse(null);

        if (cart == null || cart.getItems().isEmpty()) {
            return CartResponse.builder()
                    .items(Collections.emptyList())
                    .build();
        }

        List<CartItemResponse> itemResponses = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            ProductVariant variant = item.getVariant();
            Product product = variant.getProduct();

            boolean isAvailable = !product.getIsDeleted() && variant.getIsActive() && variant.getStockQuantity() > 0;
            boolean hasError = isAvailable && item.getQuantity() > variant.getStockQuantity();
            String errMsg = hasError ? "Số lượng sản phẩm trong kho không đủ. Hiện chỉ còn " + variant.getStockQuantity() + " sản phẩm." : null;

            if (!isAvailable) {
                errMsg = "Sản phẩm đã hết hàng hoặc ngừng kinh doanh";
            }

            itemResponses.add(CartItemResponse.builder()
                    .id(item.getId())
                    .skuCode(variant.getSkuCode())
                    .productName(product.getName())
                    .variantName(variant.getVariantName())
                    .thumbnailUrl(product.getThumbnailUrl())
                    .unitPrice(variant.getPrice())
                    .quantity(item.getQuantity())
                    .currentStock(variant.getStockQuantity())
                    .isAvailable(isAvailable)
                    .hasError(hasError)
                    .errorMessage(errMsg)
                    .build());
        }

        itemResponses.sort((a, b) -> b.getId().compareTo(a.getId()));

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(itemResponses)
                .build();
    }

    @Override
    @Transactional
    public void updateItemQuantity(Long itemId, UpdateCartItemRequest request) {
        User user = userService.getCurrentAuthenticatedUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại!"));

        CartItem item = cartItemRepository.findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng!"));

        ProductVariant variant = item.getVariant();

        if (variant.getProduct().getIsDeleted() || !variant.getIsActive()) {
            throw new RuntimeException("Sản phẩm này đã ngừng kinh doanh hoặc tạm ẩn!");
        }

        if (request.getQuantity() > variant.getStockQuantity()) {
            throw new RuntimeException("Số lượng sản phẩm trong kho không đủ (Còn: " + variant.getStockQuantity() + ")");
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        log.info("User [{}] đã cập nhật số lượng CartItem [{}] thành {}", user.getEmail(), itemId, request.getQuantity());
    }

    @Override
    @Transactional(readOnly = true)
    public MiniCartResponse getMiniCart() {
        User user = userService.getCurrentAuthenticatedUser();
        Cart cart = cartRepository.findByUser(user).orElse(null);

        if (cart == null || cart.getItems().isEmpty()) {
            return MiniCartResponse.builder()
                    .totalCartItems(0)
                    .totalTempPrice(0L)
                    .items(Collections.emptyList())
                    .build();
        }

        int totalCartItems = cart.getItems().size();
        long totalTempPrice = 0L;
        List<CartItemResponse> itemResponses = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            ProductVariant variant = item.getVariant();
            Product product = variant.getProduct();

            boolean isAvailable = !product.getIsDeleted() && variant.getIsActive() && variant.getStockQuantity() > 0;
            boolean hasError = isAvailable && item.getQuantity() > variant.getStockQuantity();

            if (isAvailable && !hasError) {
                totalTempPrice += (variant.getPrice() * item.getQuantity());
            }

            itemResponses.add(CartItemResponse.builder()
                    .id(item.getId())
                    .skuCode(variant.getSkuCode())
                    .productName(product.getName())
                    .variantName(variant.getVariantName())
                    .thumbnailUrl(product.getThumbnailUrl())
                    .unitPrice(variant.getPrice())
                    .quantity(item.getQuantity())
                    .currentStock(variant.getStockQuantity())
                    .isAvailable(isAvailable)
                    .hasError(hasError)
                    .errorMessage(!isAvailable ? "Sản phẩm đã hết hàng hoặc ngừng kinh doanh" :
                            (hasError ? "Số lượng sản phẩm trong kho không đủ" : null))
                    .build());
        }

        itemResponses.sort((a, b) -> b.getId().compareTo(a.getId()));

        List<CartItemResponse> top3Items = itemResponses.stream().limit(3).toList();

        return MiniCartResponse.builder()
                .totalCartItems(totalCartItems)
                .totalTempPrice(totalTempPrice)
                .items(top3Items)
                .build();
    }

    @Override
    @Transactional
    public void removeCartItem(Long itemId) {
        User user = userService.getCurrentAuthenticatedUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại!"));

        CartItem item = cartItemRepository.findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng!"));

        cart.getItems().remove(item);
        cartRepository.save(cart);

        log.info("User [{}] đã xóa CartItem [{}] khỏi giỏ hàng.", user.getEmail(), itemId);
    }
}