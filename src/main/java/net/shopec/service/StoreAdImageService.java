package net.shopec.service;

import java.util.List;

import net.shopec.Filter;
import net.shopec.Order;
import net.shopec.Page;
import net.shopec.Pageable;
import net.shopec.entity.Store;
import net.shopec.entity.StoreAdImage;

/**
 * Service - 店铺广告图片
 * 
 */
public interface StoreAdImageService extends BaseService<StoreAdImage> {

	/**
	 * 查找店铺广告图片
	 * 
	 * @param storeId
	 *            店铺ID
	 * @param count
	 *            数量
	 * @param filters
	 *            筛选
	 * @param orders
	 *            排序
	 * @param useCache
	 *            是否使用缓存
	 * @return 店铺广告图片
	 */
	List<StoreAdImage> findList(Long storeId, Integer count, List<Filter> filters, List<Order> orders, boolean useCache);

	/**
	 * 查找店铺广告图片分页
	 * 
	 * @param store
	 *            店铺
	 * @param pageable
	 *            分页信息
	 * @return 店铺广告图片分页
	 */
	Page<StoreAdImage> findPage(Store store, Pageable pageable);

}