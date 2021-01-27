package com.igomall.service.impl;

import java.math.BigDecimal;
import java.util.Date;

import javax.inject.Inject;

import com.igomall.Page;
import com.igomall.Pageable;
import com.igomall.dao.BusinessCashDao;
import com.igomall.dao.BusinessDao;
import com.igomall.entity.Business;
import com.igomall.entity.BusinessCash;
import com.igomall.entity.BusinessDepositLog;
import com.igomall.service.BusinessCashService;
import com.igomall.service.BusinessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;

/**
 * Service - 商家提现
 * 
 */
@Service
public class BusinessCashServiceImpl extends BaseServiceImpl<BusinessCash> implements BusinessCashService {

	@Inject
	private BusinessCashDao businessCashDao;
	@Inject
	private BusinessDao businessDao;
	@Inject
	private BusinessService businessService;

	@Override
	@Transactional(readOnly = true)
	public Page<BusinessCash> findPage(BusinessCash.Status status, String bank, String account, Business business, Pageable pageable) {
		IPage<BusinessCash> iPage = getPluginsPage(pageable);
		iPage.setRecords(businessCashDao.findPage(iPage, getPageable(pageable), status, bank, account, business));
		return super.findPage(iPage, pageable);
	}

	@Override
	public void applyCash(BusinessCash businessCash, Business business) {
		Assert.notNull(businessCash, "[Assertion failed] - businessCash is required; it must not be null");
		Assert.notNull(business, "[Assertion failed] - business is required; it must not be null");
		Assert.isTrue(businessCash.getAmount().compareTo(BigDecimal.ZERO) > 0, "[Assertion failed] - businessCash amount must be greater than 0");

		businessCash.setStatus(BusinessCash.Status.PENDING);
		businessCash.setBusiness(business);
		businessCash.setId(IdWorker.getId());
		businessCash.setCreatedDate(new Date());
		businessCash.setVersion(0L);
		businessCashDao.save(businessCash);

		businessService.addFrozenAmount(business, businessCash.getAmount());

	}

	@Override
	public void review(BusinessCash businessCash, Boolean isPassed) {
		Assert.notNull(businessCash, "[Assertion failed] - businessCash is required; it must not be null");
		Assert.notNull(isPassed, "[Assertion failed] - isPassed is required; it must not be null");

		Business business = businessCash.getBusiness();
		if (isPassed) {
			Assert.notNull(businessCash.getAmount(), "[Assertion failed] - businessCash amount is required; it must not be null");
			Assert.notNull(businessCash.getBusiness(), "[Assertion failed] - businessCash business is required; it must not be null");
			businessCash.setStatus(BusinessCash.Status.APPROVED);
			businessService.addBalance(businessCash.getBusiness(), businessCash.getAmount().negate(), BusinessDepositLog.Type.CASH, null);
		} else {
			businessCash.setStatus(BusinessCash.Status.FAILED);
		}
		businessCashDao.update(businessCash);
		businessService.addFrozenAmount(business, businessCash.getAmount().negate());
	}

	@Override
	@Transactional(readOnly = true)
	public Long count(Business business, BusinessCash.Status status, String bank, String account) {
		return businessCashDao.count(status, bank, account, business);
	}

	@Override
	public Long count(Long businessId, BusinessCash.Status status, String bank, String account) {
		Business business = businessDao.find(businessId);
		if (businessId != null && business == null) {
			return 0L;
		}

		return businessCashDao.count(status, bank, account, business);
	}

}