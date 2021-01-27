package com.igomall.api.controller.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.igomall.service.CartService;
import com.igomall.service.SkuService;
import com.igomall.util.SystemUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.igomall.Setting;
import com.igomall.api.model.ApiResult;
import com.igomall.api.util.ResultUtils;
import com.igomall.entity.Cart;
import com.igomall.entity.CartItem;
import com.igomall.entity.Product;
import com.igomall.entity.Sku;
import com.igomall.entity.Store;
import com.igomall.security.CurrentCart;

/**
 * 购物车 - 接口类
 */
@RestController
@RequestMapping("/api/cart")
public class CartAPIController {

	@Inject
	private SkuService skuService;
	@Inject
	private CartService cartService;

	/**
	 * 获取当前购物车
	 */
	@GetMapping("/get_current")
	public ApiResult getCurrent(@CurrentCart Cart currentCart) {
		Map<String, Object> data = new HashMap<>();
		List<Map<String, Object>> cartItems = new ArrayList<>();
		Setting setting = SystemUtils.getSetting();
		if (currentCart != null && !currentCart.isEmpty()) {
			data.put("qty", currentCart.getQuantity(false));
			data.put("rewardPoint", currentCart.getRewardPoint());
			data.put("effectivePrice", currentCart.getEffectivePrice());
			data.put("discount", currentCart.getDiscount());
			for (CartItem cartItem : currentCart) {
				Map<String, Object> item = new HashMap<>();
				Sku sku = cartItem.getSku();
				Product product = sku.getProduct();
				item.put("productId", product.getId());
				item.put("skuId", sku.getId());
				item.put("skuName", sku.getName());
				item.put("skuThumbnail", sku.getThumbnail() != null ? setting.getSiteUrl() + sku.getThumbnail() : setting.getSiteUrl() + setting.getDefaultThumbnailProductImage());
				item.put("price", cartItem.getPrice());
				item.put("specifications", sku.getSpecifications());
				item.put("quantity", cartItem.getQuantity());
				item.put("subtotal", cartItem.getSubtotal());
				cartItems.add(item);
			}
		}
		data.put("cartItems", cartItems);
		return ResultUtils.ok(data);
	}

	/**
	 * 添加
	 */
	@PostMapping("/add")
	public ApiResult add(Long skuId, Integer quantity, @CurrentCart Cart currentCart) {
		Map<String, Object> data = new HashMap<>();
		if (quantity == null || quantity < 1) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		Sku sku = skuService.find(skuId);
		if (sku == null) {
			return ResultUtils.unprocessableEntity("shop.cart.skuNotExist");
		}
		if (!Product.Type.GENERAL.equals(sku.getType())) {
			return ResultUtils.unprocessableEntity("shop.cart.skuNotForSale");
		}
		if (!sku.getIsActive()) {
			return ResultUtils.unprocessableEntity("shop.cart.skuNotActive");
		}
		if (!sku.getIsMarketable()) {
			return ResultUtils.unprocessableEntity("shop.cart.skuNotMarketable");
		}
		if (sku.getProduct().getStore().hasExpired()) {
			return ResultUtils.unprocessableEntity("shop.cart.skuNotBuyExpired");
		}

		int cartItemSize = 1;
		int skuQuantity = quantity;
		if (currentCart != null) {
			if (currentCart.contains(sku)) {
				CartItem cartItem = currentCart.getCartItem(sku);
				cartItemSize = currentCart.size();
				skuQuantity = cartItem.getQuantity() + quantity;
			} else {
				cartItemSize = currentCart.size() + 1;
				skuQuantity = quantity;
			}
		}
		if (Cart.MAX_CART_ITEM_SIZE != null && cartItemSize > Cart.MAX_CART_ITEM_SIZE) {
			return ResultUtils.unprocessableEntity("shop.cart.addCartItemSizeNotAllowed", Cart.MAX_CART_ITEM_SIZE);
		}
		if (CartItem.MAX_QUANTITY != null && skuQuantity > CartItem.MAX_QUANTITY) {
			return ResultUtils.unprocessableEntity("shop.cart.addQuantityNotAllowed", CartItem.MAX_QUANTITY);
		}
		if (skuQuantity > sku.getAvailableStock()) {
			return ResultUtils.unprocessableEntity("shop.cart.skuLowStock");
		}
		if (currentCart == null) {
			currentCart = cartService.create();
		}
		cartService.add(currentCart, sku, quantity);
		data.put("qty", currentCart.getQuantity(false));
		data.put("rewardPoint", currentCart.getRewardPoint());
		data.put("effectivePrice", currentCart.getEffectivePrice());
		data.put("discount", currentCart.getDiscount());
		data.put("cartKey", currentCart.getKey());
		return ResultUtils.ok(data);
	}
	
	/**
	 * 修改
	 */
	@PostMapping("/modify")
	public ApiResult modify(Long skuId, Integer quantity, @CurrentCart Cart currentCart) {
		Map<String, Object> data = new HashMap<>();
		if (quantity == null || quantity < 1) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		Sku sku = skuService.find(skuId);
		if (sku == null) {
			return ResultUtils.unprocessableEntity("shop.cart.skuNotExist");
		}
		if (currentCart == null || currentCart.isEmpty()) {
			return ResultUtils.unprocessableEntity("shop.cart.notEmpty");
		}
		if (!currentCart.contains(sku)) {
			return ResultUtils.unprocessableEntity("shop.cart.cartItemNotExist");
		}
		if (!sku.getIsActive()) {
			return ResultUtils.unprocessableEntity("shop.cart.skuNotActive");
		}
		if (!sku.getIsMarketable()) {
			return ResultUtils.unprocessableEntity("shop.cart.skuNotMarketable");
		}
		if (CartItem.MAX_QUANTITY != null && quantity > CartItem.MAX_QUANTITY) {
			return ResultUtils.unprocessableEntity("shop.cart.addQuantityNotAllowed", CartItem.MAX_QUANTITY);
		}
		if (quantity > sku.getAvailableStock()) {
			return ResultUtils.unprocessableEntity("shop.cart.skuLowStock");
		}
		cartService.modify(currentCart, sku, quantity);

		data.put("qty", currentCart.getQuantity(false));
		data.put("rewardPoint", currentCart.getRewardPoint());
		data.put("effectivePrice", currentCart.getEffectivePrice());
		data.put("discount", currentCart.getDiscount());

		Map<String, Object> cartItemMap = new HashMap<>();
		CartItem cartItem = currentCart.getCartItem(sku);
		cartItemMap.put("subtotal", cartItem.getSubtotal());
		cartItemMap.put("isLowStock", cartItem.getIsLowStock());
		data.put("cartItem", cartItemMap);

		Store store = sku.getStore();
		Map<String, Object> storeMap = new HashMap<>();
		storeMap.put("giftNames", currentCart.getGiftNames(store));
		storeMap.put("promotionNames", currentCart.getPromotionNames(store));
		data.put("store", storeMap);

		return ResultUtils.ok(data);
	}
	
	/**
	 * 移除
	 */
	@PostMapping("/remove")
	public ApiResult remove(Long skuId, @CurrentCart Cart currentCart) {
		Map<String, Object> data = new HashMap<>();
		Sku sku = skuService.find(skuId);
		if (sku == null) {
			return ResultUtils.unprocessableEntity("shop.cart.skuNotExist");
		}
		if (currentCart == null || currentCart.isEmpty()) {
			return ResultUtils.unprocessableEntity("shop.cart.notEmpty");
		}
		if (!currentCart.contains(sku)) {
			return ResultUtils.unprocessableEntity("shop.cart.cartItemNotExist");
		}
		Store store = sku.getProduct().getStore();
		cartService.remove(currentCart, sku);

		data.put("qty", currentCart.getQuantity(false));
		data.put("rewardPoint", currentCart.getRewardPoint());
		data.put("effectivePrice", currentCart.getEffectivePrice());
		data.put("discount", currentCart.getDiscount());

		Map<String, Object> storeMap = new HashMap<>();
		storeMap.put("giftNames", currentCart.getGiftNames(store));
		storeMap.put("promotionNames", currentCart.getPromotionNames(store));
		data.put("store", storeMap);
		return ResultUtils.ok(data);
	}
	
	/**
	 * 清空
	 */
	@PostMapping("/clear")
	public ApiResult clear(@CurrentCart Cart currentCart) {
		if (currentCart != null) {
			cartService.clear(currentCart);
		}
		return ResultUtils.ok();
	}
	
}
