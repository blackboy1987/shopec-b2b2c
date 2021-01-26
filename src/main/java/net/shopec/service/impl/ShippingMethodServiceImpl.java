package net.shopec.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import net.shopec.Setting;
import net.shopec.dao.DefaultFreightConfigDao;
import net.shopec.dao.ShippingMethodDao;
import net.shopec.entity.Area;
import net.shopec.entity.AreaFreightConfig;
import net.shopec.entity.DefaultFreightConfig;
import net.shopec.entity.Receiver;
import net.shopec.entity.ShippingMethod;
import net.shopec.entity.Store;
import net.shopec.service.ShippingMethodService;
import net.shopec.util.SystemUtils;

/**
 * Service - 配送方式
 * 
 */
@Service
public class ShippingMethodServiceImpl extends BaseServiceImpl<ShippingMethod> implements ShippingMethodService {

	@Inject
	private DefaultFreightConfigDao defaultFreightConfigDao;
	
	@Inject
	private ShippingMethodDao shippingMethodDao;

	
	@Override
	@Transactional(readOnly = true)
	public BigDecimal calculateFreight(ShippingMethod shippingMethod, Store store, Area area, Integer weight) {
		Assert.notNull(shippingMethod, "[Assertion failed] - shippingMethod is required; it must not be null");

		Setting setting = SystemUtils.getSetting();
		DefaultFreightConfig defaultFreightConfig = defaultFreightConfigDao.findDefault(shippingMethod, store);
		BigDecimal firstPrice = defaultFreightConfig != null ? defaultFreightConfig.getFirstPrice() : BigDecimal.ZERO;
		BigDecimal continuePrice = defaultFreightConfig != null ? defaultFreightConfig.getContinuePrice() : BigDecimal.ZERO;
		Integer firstWeight = defaultFreightConfig != null ? defaultFreightConfig.getFirstWeight() : 0;
		Integer continueWeight = defaultFreightConfig != null ? defaultFreightConfig.getContinueWeight() : 1;
		if (area != null && CollectionUtils.isNotEmpty(shippingMethod.getAreaFreightConfigs())) {
			List<Area> areas = new ArrayList<>();
			areas.addAll(area.getParents());
			areas.add(area);
			for (int i = areas.size() - 1; i >= 0; i--) {
				AreaFreightConfig areaFreightConfig = shippingMethod.getAreaFreightConfig(store, areas.get(i));
				if (areaFreightConfig != null) {
					firstPrice = areaFreightConfig.getFirstPrice();
					continuePrice = areaFreightConfig.getContinuePrice();
					firstWeight = areaFreightConfig.getFirstWeight();
					continueWeight = areaFreightConfig.getContinueWeight();
					break;
				}
			}
		}
		if (weight == null || weight <= firstWeight || continuePrice.compareTo(BigDecimal.ZERO) == 0) {
			return setting.setScale(firstPrice);
		} else {
			double contiuneWeightCount = Math.ceil((weight - firstWeight) / (double) continueWeight);
			return setting.setScale(firstPrice.add(continuePrice.multiply(new BigDecimal(String.valueOf(contiuneWeightCount)))));
		}
	}

	@Override
	@Transactional(readOnly = true)
	public BigDecimal calculateFreight(ShippingMethod shippingMethod, Store store, Receiver receiver, Integer weight) {
		return calculateFreight(shippingMethod, store, receiver != null ? receiver.getArea() : null, weight);
	}

	@Override
	@Transactional
	public boolean save(ShippingMethod shippingMethod) {
		return super.save(shippingMethod);
	}
	
	@Override
	@Transactional
	public ShippingMethod update(ShippingMethod shippingMethod) {
		return super.update(shippingMethod);
	}

	@Override
	@Transactional
	public ShippingMethod update(ShippingMethod shippingMethod, String... ignoreProperties) {
		return super.update(shippingMethod, ignoreProperties);
	}
	
	@Override
	@Transactional
	public void delete(Long id) {
		shippingMethodDao.delete(Arrays.asList(id));
	}

	@Override
	@Transactional
	public void delete(Long... ids) {
		shippingMethodDao.delete(Arrays.asList(ids));
	}

	@Override
	@Transactional
	public void delete(ShippingMethod shippingMethod) {
		shippingMethodDao.delete(Arrays.asList(shippingMethod.getId()));
	}
	
	
}