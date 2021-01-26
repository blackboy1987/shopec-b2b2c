package net.shopec.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import net.shopec.dao.AftersalesSettingDao;
import net.shopec.entity.AftersalesSetting;
import net.shopec.entity.Store;
import net.shopec.service.AftersalesSettingService;

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