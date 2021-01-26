package net.shopec.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import net.shopec.entity.OrderReturns;
import net.shopec.entity.Sn;
import net.shopec.service.OrderReturnsService;
import net.shopec.service.SnService;

/**
 * Service - 订单退货
 * 
 */
@Service
public class OrderReturnsServiceImpl extends BaseServiceImpl<OrderReturns> implements OrderReturnsService {

	@Inject
	private SnService snService;

	@Override
	@Transactional
	public boolean save(OrderReturns orderReturns) {
		Assert.notNull(orderReturns, "[Assertion failed] - orderReturns is required; it must not be null");

		orderReturns.setSn(snService.generate(Sn.Type.ORDER_RETURNS));

		return super.save(orderReturns);
	}

}