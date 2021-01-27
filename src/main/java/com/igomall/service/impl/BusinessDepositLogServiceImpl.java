package com.igomall.service.impl;

import javax.inject.Inject;

import com.igomall.Page;
import com.igomall.Pageable;
import com.igomall.dao.BusinessDepositLogDao;
import com.igomall.entity.Business;
import com.igomall.entity.BusinessDepositLog;
import com.igomall.service.BusinessDepositLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * Service - 商家预存款记录
 * 
 */
@Service
public class BusinessDepositLogServiceImpl extends BaseServiceImpl<BusinessDepositLog> implements BusinessDepositLogService {

	@Inject
	private BusinessDepositLogDao businessDepositLogDao;

	@Override
	@Transactional(readOnly = true)
	public Page<BusinessDepositLog> findPage(Business business, Pageable pageable) {
		IPage<BusinessDepositLog> iPage = getPluginsPage(pageable);
		iPage.setRecords(businessDepositLogDao.findPage(iPage, getPageable(pageable), business));
		return super.findPage(iPage, pageable);
	}

}