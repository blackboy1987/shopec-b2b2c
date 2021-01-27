package com.igomall.api.controller.shop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.igomall.service.ProductCategoryService;
import com.igomall.service.ProductService;
import com.igomall.service.StoreService;
import com.igomall.util.SystemUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.igomall.Page;
import com.igomall.Pageable;
import com.igomall.Setting;
import com.igomall.api.model.ApiResult;
import com.igomall.api.util.ResultUtils;
import com.igomall.entity.Member;
import com.igomall.entity.Product;
import com.igomall.entity.ProductCategory;
import com.igomall.entity.ProductImage;
import com.igomall.entity.Review;
import com.igomall.entity.Sku;
import com.igomall.entity.SpecificationItem;
import com.igomall.entity.Store;

/**
 * 商品 - 接口类
 */
@RestController
@RequestMapping("/api/product")
public class ProductAPIController {

	/**
	 * 最大浏览记录商品数
	 */
	public static final Integer MAX_HISTORY_PRODUCT_COUNT = 10;
	
	@Inject
	private ProductService productService;
	@Inject
	private StoreService storeService;
	@Inject
	private ProductCategoryService productCategoryService;
	
	/**
	 * 列表
	 */
	@GetMapping("/list")
	public ApiResult list(Long productCategoryId, Product.OrderType orderType, Integer pageNumber, Integer pageSize) {
		ProductCategory productCategory = productCategoryService.find(productCategoryId);

		Setting setting = SystemUtils.getSetting();
		Pageable pageable = new Pageable(pageNumber, pageSize);
		Page<Product> pages = productService.findPage(null, null, null, productCategory, null, null, null, null, null, null, null, null, true, true, null, true, null, null, null, orderType, pageable);
		Map<String, Object> data = new HashMap<>();
		List<Map<String, Object>> productItems = new ArrayList<>();
		for (Product product : pages.getContent()) {
			Map<String, Object> item = new HashMap<>();
			item.put("id", String.valueOf(product.getId()));
			item.put("name", product.getName());
			item.put("price", product.getPrice());
			item.put("exchangePoint", product.getExchangePoint());
			item.put("marketPrice", product.getMarketPrice());
			item.put("thumbnail", setting.getSiteUrl() + product.getThumbnail());
			productItems.add(item);
		}
		
		data.put("pageNumber", pages.getPageNumber());
		data.put("pageSize", pages.getPageSize());
		data.put("lastPage", pages.isLastPage());
		data.put("total", pages.getTotal());
		data.put("productItems", productItems);
		return ResultUtils.ok(data);
	}
	
	/**
	 * 详情
	 */
	@GetMapping("/detail")
	public ApiResult detail(Long productId) {
		Product product = productService.find(productId);
		if (product == null || BooleanUtils.isNotTrue(product.getIsActive()) || BooleanUtils.isNotTrue(product.getIsMarketable())) {
			return ResultUtils.NOT_FOUND;
		}
		Map<String, Object> data = new HashMap<>();
		Setting setting = SystemUtils.getSetting();
		
		Map<String, Object> detail = new HashMap<>();
		detail.put("id", String.valueOf(product.getId()));
		detail.put("name", product.getName());
		detail.put("price", product.getPrice());
		detail.put("marketPrice", product.getMarketPrice());
		detail.put("exchangePoint", product.getExchangePoint());
		detail.put("thumbnail", product.getThumbnail());
		// 订单详情
		String introduction = product.getIntroduction();
		int index = 0;
		while ((index = introduction.indexOf("src=\"", index)) != -1) {
			index = index + "src=\"".length();
			StringBuilder sb = new StringBuilder(introduction);
			introduction = String.valueOf(sb.insert(index, setting.getSiteUrl()));// 插入
		}
		detail.put("introduction", introduction);
		List<ProductImage> productImages = product.getProductImages();
		for (ProductImage productImage : productImages) {
			productImage.setMedium(setting.getSiteUrl() + productImage.getMedium());
		}
		detail.put("productImages", productImages);
		detail.put("sales", product.getSales());
		detail.put("hasSpecification", product.hasSpecification());
		
		// 默认sku
		Sku defaultSku = product.getDefaultSku();
		Map<String, Object> dSku = new HashMap<>();
		dSku.put("skuId", String.valueOf(defaultSku.getId()));
		dSku.put("marketPrice", defaultSku.getMarketPrice());
		dSku.put("exchangePoint", defaultSku.getExchangePoint());
		dSku.put("price", defaultSku.getPrice());
		dSku.put("stock", defaultSku.getStock());
		dSku.put("specificationValues", defaultSku.getSpecificationValues());
		detail.put("defaultSku", dSku);
		
		// 多规格sku
		Map<String, Object> specData = new HashMap<>();
		List<Map<String, Object>> skus = new ArrayList<>();
		for (Sku sku : product.getSkus()) {
			Map<String, Object> item = new HashMap<>();
			item.put("skuId", String.valueOf(sku.getId()));
			item.put("skuName", sku.getName());
			item.put("skuThumbnail", sku.getThumbnail() != null ? sku.getThumbnail() : setting.getDefaultThumbnailProductImage());
			item.put("price", sku.getPrice());
			item.put("specificationValues", sku.getSpecificationValues());
			item.put("marketPrice", defaultSku.getMarketPrice());
			item.put("exchangePoint", defaultSku.getExchangePoint());
			item.put("stock", defaultSku.getStock());
			List<String> specSkus = new ArrayList<>();
			for (Integer i : sku.getSpecificationValueIds()) {
				specSkus.add(String.valueOf(i));
			}
			item.put("specSkuId", String.join("_", specSkus));
			skus.add(item);
		}
		specData.put("skus", skus);
		// 查出有效规格项
		for (SpecificationItem specificationItem : product.getSpecificationItems()) {
			List<SpecificationItem.Entry> entries = specificationItem.getEntries();
			List<SpecificationItem.Entry> pEntries = new ArrayList<>();
			for (SpecificationItem.Entry entrie : entries) {
				if (entrie.getIsSelected() == Boolean.TRUE) {
					pEntries.add(entrie);
				}
			}
			specificationItem.setEntries(pEntries);
		}
		specData.put("specificationItems", product.getSpecificationItems());
		
		// 评价
		List<Map<String, Object>> reviews = new ArrayList<>();
		for (Review review : product.getReviews()) {
			Member member = review.getMember();
			Map<String, Object> item = new HashMap<>();
			item.put("username", member.getUsername());
			item.put("score", review.getScore());
			item.put("content", review.getContent());
			item.put("createdDate", review.getCreatedDate());
			reviews.add(item);
		}
		
		data.put("reviews", reviews);
		data.put("specData", specData);
		data.put("detail", detail);
		return ResultUtils.ok(data);
	}
	
	
	
	/**
	 * 搜索
	 */
	@GetMapping("/search")
	public ApiResult search(@RequestParam("keyword") String keyword, Long storeId, BigDecimal startPrice,
			BigDecimal endPrice, Product.OrderType orderType, Integer pageNumber, Integer pageSize) {
		if (StringUtils.isEmpty(keyword)) {
			return ResultUtils.NOT_FOUND;
		}

		if (startPrice != null && endPrice != null && startPrice.compareTo(endPrice) > 0) {
			BigDecimal tempPrice = startPrice;
			startPrice = endPrice;
			endPrice = tempPrice;
		}
		Store store = storeService.find(storeId);

		Pageable pageable = new Pageable(pageNumber, pageSize);
		List<Map<String, Object>> data = new ArrayList<>();
		List<Product> products = productService.search(keyword, null, null, store, null, null, startPrice, endPrice, orderType, pageable).getContent();
		for (Product product : products) {
			Map<String, Object> item = new HashMap<>();
			item.put("id", product.getId());
			item.put("name", product.getName());
			item.put("price", product.getPrice());
			item.put("thumbnail", product.getThumbnail());
			data.add(item);
		}
		return ResultUtils.ok(data);
	}
	
}
