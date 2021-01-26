package net.shopec.service.impl;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.shopec.dao.StoreCategoryDao;
import net.shopec.entity.StoreCategory;
import net.shopec.service.StoreCategoryService;

/**
 * Service - 店铺分类
 * 
 */
@Service
public class StoreCategoryServiceImpl extends BaseServiceImpl<StoreCategory> implements StoreCategoryService {

	@Inject
	private StoreCategoryDao storeCategoryDao;

	@Override
	@Transactional(readOnly = true)
	public boolean nameExists(String name) {
		return storeCategoryDao.exists("name", name);
	}

	@Override
	@Transactional(readOnly = true)
	public List<StoreCategory> findAll() {
		return storeCategoryDao.findAll();
	}
}