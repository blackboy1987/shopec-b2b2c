package net.shopec.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;

import net.shopec.Page;
import net.shopec.Pageable;
import net.shopec.dao.BusinessDepositLogDao;
import net.shopec.entity.Business;
import net.shopec.entity.BusinessDepositLog;
import net.shopec.service.BusinessDepositLogService;

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