package net.shopec.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.baomidou.mybatisplus.core.metadata.IPage;

import net.shopec.Page;
import net.shopec.Pageable;
import net.shopec.dao.DefaultFreightConfigDao;
import net.shopec.entity.Area;
import net.shopec.entity.DefaultFreightConfig;
import net.shopec.entity.ShippingMethod;
import net.shopec.entity.Store;
import net.shopec.service.DefaultFreightConfigService;

/**
 * Service - 默认运费配置
 * 
 */
@Service
public class DefaultFreightConfigServiceImpl extends BaseServiceImpl<DefaultFreightConfig> implements DefaultFreightConfigService {

	@Inject
	private DefaultFreightConfigDao defaultFreightConfigDao;
	
	@Override
	public DefaultFreightConfig find(Long id) {
		return defaultFreightConfigDao.find(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public boolean exists(ShippingMethod shippingMethod, Area area) {
		return defaultFreightConfigDao.exists(shippingMethod, area);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean unique(ShippingMethod shippingMethod, Area previousArea, Area currentArea) {
		return (previousArea != null && previousArea.equals(currentArea)) || !defaultFreightConfigDao.exists(shippingMethod, currentArea);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<DefaultFreightConfig> findPage(Store store, Pageable pageable) {
		IPage<DefaultFreightConfig> iPage = getPluginsPage(pageable);
		iPage.setRecords(defaultFreightConfigDao.findPage(iPage, getPageable(pageable), store));
		return super.findPage(iPage, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public DefaultFreightConfig find(ShippingMethod shippingMethod, Store store) {
		return defaultFreightConfigDao.findDefault(shippingMethod, store);
	}

	@Override
	public void update(DefaultFreightConfig defaultFreightConfig, Store store, ShippingMethod shippingMethod) {
		Assert.notNull(store, "[Assertion failed] - store is required; it must not be null");
		Assert.notNull(shippingMethod, "[Assertion failed] - shippingMethod is required; it must not be null");
		if (!defaultFreightConfig.isNew()) {
			super.update(defaultFreightConfig);
		} else {
			defaultFreightConfig.setStore(store);
			defaultFreightConfig.setShippingMethod(shippingMethod);
			super.save(defaultFreightConfig);
		}
	}

}