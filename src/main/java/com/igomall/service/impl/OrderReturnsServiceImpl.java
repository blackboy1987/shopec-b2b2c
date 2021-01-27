package com.igomall.service.impl;

import javax.inject.Inject;

import com.igomall.entity.OrderReturns;
import com.igomall.entity.Sn;
import com.igomall.service.OrderReturnsService;
import com.igomall.service.SnService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

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