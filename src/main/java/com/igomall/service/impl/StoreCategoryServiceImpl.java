package com.igomall.service.impl;

import java.util.List;

import javax.inject.Inject;

import com.igomall.dao.StoreCategoryDao;
import com.igomall.entity.StoreCategory;
import com.igomall.service.StoreCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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