package com.igomall.service.impl;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.igomall.Filter;
import com.igomall.Order;
import com.igomall.dao.BrandDao;
import com.igomall.dao.ProductCategoryDao;
import com.igomall.entity.Brand;
import com.igomall.entity.ProductCategory;
import com.igomall.service.BrandService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * Service - 品牌
 * 
 */
@Service
public class BrandServiceImpl extends BaseServiceImpl<Brand> implements BrandService {

	@Inject
	private BrandDao brandDao;
	@Inject
	private ProductCategoryDao productCategoryDao;

	@Override
	@Transactional(readOnly = true)
	public List<Brand> findList(ProductCategory productCategory, Integer count, List<Filter> filters, List<Order> orders) {
		QueryWrapper<Brand> queryWrapper = createQueryWrapper(null, count, filters, orders);
		return brandDao.findList(queryWrapper, productCategory);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "brand", condition = "#useCache")
	public List<Brand> findList(Long productCategoryId, Integer count, List<Filter> filters, List<Order> orders, boolean useCache) {
		ProductCategory productCategory = productCategoryDao.find(productCategoryId);
		if (productCategoryId != null && productCategory == null) {
			return Collections.emptyList();
		}
		QueryWrapper<Brand> queryWrapper = createQueryWrapper(null, count, filters, orders);
		return brandDao.findList(queryWrapper, productCategory);
	}

	@Override
	@Transactional
	@CacheEvict(value = "brand", allEntries = true)
	public boolean save(Brand brand) {
		return super.save(brand);
	}

	@Override
	@Transactional
	@CacheEvict(value = "brand", allEntries = true)
	public Brand update(Brand brand) {
		return super.update(brand);
	}

	@Override
	@Transactional
	@CacheEvict(value = "brand", allEntries = true)
	public Brand update(Brand brand, String... ignoreProperties) {
		return super.update(brand, ignoreProperties);
	}

	@Override
	@Transactional
	@CacheEvict(value = "brand", allEntries = true)
	public void delete(Long id) {
		super.delete(id);
	}

	@Override
	@Transactional
	@CacheEvict(value = "brand", allEntries = true)
	public void delete(Long... ids) {
		super.delete(ids);
	}

	@Override
	@Transactional
	@CacheEvict(value = "brand", allEntries = true)
	public void delete(Brand brand) {
		super.delete(brand);
	}

}