package com.igomall.service.impl;

import javax.inject.Inject;

import com.igomall.Page;
import com.igomall.Pageable;
import com.igomall.dao.AreaFreightConfigDao;
import com.igomall.entity.Area;
import com.igomall.entity.AreaFreightConfig;
import com.igomall.entity.ShippingMethod;
import com.igomall.entity.Store;
import com.igomall.service.AreaFreightConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * Service - 地区运费配置
 * 
 */
@Service
public class AreaFreightConfigServiceImpl extends BaseServiceImpl<AreaFreightConfig> implements AreaFreightConfigService {

	@Inject
	private AreaFreightConfigDao areaFreightConfigDao;

	@Override
	@Transactional(readOnly = true)
	public boolean exists(ShippingMethod shippingMethod, Store store, Area area) {
		return areaFreightConfigDao.exists(shippingMethod, store, area);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean unique(Long id, ShippingMethod shippingMethod, Store store, Area area) {
		return areaFreightConfigDao.unique(id, shippingMethod, store, area);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AreaFreightConfig> findPage(ShippingMethod shippingMethod, Store store, Pageable pageable) {
		IPage<AreaFreightConfig> iPage = getPluginsPage(pageable);
		iPage.setRecords(areaFreightConfigDao.findPage(iPage, getPageable(pageable), shippingMethod, store));
		return super.findPage(iPage, pageable);
	}

}