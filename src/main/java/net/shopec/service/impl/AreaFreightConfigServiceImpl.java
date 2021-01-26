package net.shopec.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;

import net.shopec.Page;
import net.shopec.Pageable;
import net.shopec.dao.AreaFreightConfigDao;
import net.shopec.entity.Area;
import net.shopec.entity.AreaFreightConfig;
import net.shopec.entity.ShippingMethod;
import net.shopec.entity.Store;
import net.shopec.service.AreaFreightConfigService;

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