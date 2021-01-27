package com.igomall.service.impl;

import javax.inject.Inject;

import com.igomall.dao.AftersalesSettingDao;
import com.igomall.entity.AftersalesSetting;
import com.igomall.entity.Store;
import com.igomall.service.AftersalesSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Service - 售后设置
 * 
 */
@Service
public class AftersalesSettingServiceImpl extends BaseServiceImpl<AftersalesSetting> implements AftersalesSettingService {

	@Inject
	private AftersalesSettingDao aftersalesSettingDao;

	@Override
	@Transactional(readOnly = true)
	public AftersalesSetting findByStore(Store store) {
		Assert.notNull(store, "[Assertion failed] - store is required; it must not be null");

		return aftersalesSettingDao.findByAttribute("store_id", store.getId());
	}

}