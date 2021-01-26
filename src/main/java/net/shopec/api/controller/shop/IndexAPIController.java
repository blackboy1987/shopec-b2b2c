package net.shopec.api.controller.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.shopec.Setting;
import net.shopec.api.model.ApiResult;
import net.shopec.api.util.ResultUtils;
import net.shopec.entity.Ad;
import net.shopec.entity.AdPosition;
import net.shopec.entity.Product;
import net.shopec.entity.ProductCategory;
import net.shopec.service.AdPositionService;
import net.shopec.service.ProductCategoryService;
import net.shopec.service.ProductService;
import net.shopec.util.SystemUtils;

/**
 * 首页 - 接口类
 */
@RestController
@RequestMapping("/api/index")
public class IndexAPIController {
	
	@Inject
	private AdPositionService adPositionService;
	@Inject
	private ProductService productService;
	@Inject
	private ProductCategoryService productCategoryService;
	
	/**
	 * 首页
	 */
	@GetMapping
	public ApiResult index() {
		Map<String, Object> data = new HashMap<>();
		// 主轮播广告
		List<Map<String, Object>> ads = new ArrayList<>();
		Setting setting = SystemUtils.getSetting();
		AdPosition adPosition = adPositionService.find(18L);
		for (Ad ad : adPosition.getAds()) {
			Map<String, Object> item = new HashMap<>();
			if (ad.getType() == Ad.Type.IMAGE && ad.hasBegun() && !ad.hasEnded()) {
				item.put("url", ad.getUrl());
				item.put("path", setting.getSiteUrl() + ad.getPath());
				item.put("title", ad.getTitle());
				ads.add(item);
			}
		}
		// 主页产品分类
		List<Map<String, Object>> mapProductCategories = new ArrayList<>();
		List<ProductCategory> productCategories = productCategoryService.findRoots(5, true);
		for (ProductCategory productCategory : productCategories) {
			List<Product> products = productService.findList(Product.Type.GENERAL, null, productCategory.getId(), null, null, null, 1L, null, null, null, null, true, true, null, true, null, null, null, null, 6, null, null, true);
			Map<String, Object> mapProductCategory = new HashMap<>();
			mapProductCategory.put("name", productCategory.getName());
			List<Map<String, Object>> mapProducts = new ArrayList<>();
			for (Product product : products) {
				Map<String, Object> item = new HashMap<>();
				item.put("id", String.valueOf(product.getId()));
				item.put("name", product.getName());
				item.put("price", product.getPrice());
				item.put("marketPrice", product.getMarketPrice());
				item.put("thumbnail", setting.getSiteUrl() + product.getThumbnail());
				mapProducts.add(item);
			}
			mapProductCategory.put("products", mapProducts);
			mapProductCategories.add(mapProductCategory);
		}
		
		// 猜您喜欢
		List<Map<String, Object>> likes = new ArrayList<>();
		List<Product> likeProducts = productService.findList(Product.Type.GENERAL, null, null, null, null, null, 2L, null, null, null, null, true, true, null, true, null, null, null, null, 6, null, null, true);
		for (Product likeProduct : likeProducts) {
			Map<String, Object> item = new HashMap<>();
			item.put("id", String.valueOf(likeProduct.getId()));
			item.put("name", likeProduct.getName());
			item.put("price", likeProduct.getPrice());
			item.put("marketPrice", likeProduct.getMarketPrice());
			item.put("thumbnail", setting.getSiteUrl() + likeProduct.getThumbnail());
			likes.add(item);
		}
		data.put("ads", ads);
		data.put("likes", likes);
		data.put("productCategories", mapProductCategories);
		return ResultUtils.ok(data);
	}

}
