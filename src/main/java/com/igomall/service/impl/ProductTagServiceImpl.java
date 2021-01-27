package com.igomall.service.impl;

import java.util.List;

import com.igomall.Filter;
import com.igomall.Order;
import com.igomall.entity.ProductTag;
import com.igomall.service.ProductTagService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service - 商品标签
 * 
 */
@Service
public class ProductTagServiceImpl extends BaseServiceImpl<ProductTag> implements ProductTagService {

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "productTag", condition = "#useCache")
	public List<ProductTag> findList(Integer count, List<Filter> filters, List<Order> orders, boolean useCache) {
		return super.findList(null, count, filters, orders);
	}

	@Override
	@Transactional
	@CacheEvict(value = "productTag", allEntries = true)
	public boolean save(ProductTag productTag) {
		return super.save(productTag);
	}

	@Override
	@Transactional
	@CacheEvict(value = "productTag", allEntries = true)
	public ProductTag update(ProductTag productTag) {
		return super.update(productTag);
	}

	@Override
	@Transactional
	@CacheEvict(value = "productTag", allEntries = true)
	public ProductTag update(ProductTag productTag, String... ignoreProperties) {
		return super.update(productTag, ignoreProperties);
	}

	@Override
	@Transactional
	@CacheEvict(value = "productTag", allEntries = true)
	public void delete(Long id) {
		super.delete(id);
	}

	@Override
	@Transactional
	@CacheEvict(value = "productTag", allEntries = true)
	public void delete(Long... ids) {
		super.delete(ids);
	}

	@Override
	@Transactional
	@CacheEvict(value = "productTag", allEntries = true)
	public void delete(ProductTag productTag) {
		super.delete(productTag);
	}

}