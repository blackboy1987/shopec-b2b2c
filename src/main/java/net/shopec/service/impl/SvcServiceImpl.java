package net.shopec.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import net.shopec.Order;
import net.shopec.dao.SvcDao;
import net.shopec.entity.Sn;
import net.shopec.entity.Store;
import net.shopec.entity.StoreRank;
import net.shopec.entity.Svc;
import net.shopec.service.SnService;
import net.shopec.service.SvcService;

/**
 * Service - 服务
 * 
 */
@Service
public class SvcServiceImpl extends BaseServiceImpl<Svc> implements SvcService {

	@Inject
	private SvcDao svcDao;
	@Inject
	private SnService snService;

	@Override
	@Transactional(readOnly = true)
	public Svc findBySn(String sn) {
		return svcDao.findByAttribute("sn", StringUtils.lowerCase(sn));
	}

	@Override
	@Transactional(readOnly = true)
	public Svc findTheLatest(Store store, String promotionPluginId, StoreRank storeRank) {

		List<Order> orderList = new ArrayList<>();
		orderList.add(new Order("createdDate", Order.Direction.DESC));
		List<Svc> serviceOrders = svcDao.findByStore(store, promotionPluginId, storeRank);

		return CollectionUtils.isNotEmpty(serviceOrders) ? serviceOrders.get(0) : null;
	}

	@Override
	@Transactional
	public boolean save(Svc svc) {
		Assert.notNull(svc, "[Assertion failed] - svc is required; it must not be null");

		svc.setSn(snService.generate(Sn.Type.PLATFORM_SERVICE));

		return super.save(svc);
	}

}