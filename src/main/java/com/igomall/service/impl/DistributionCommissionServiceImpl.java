package com.igomall.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.igomall.Page;
import com.igomall.Pageable;
import com.igomall.dao.DistributionCommissionDao;
import com.igomall.entity.DistributionCommission;
import com.igomall.entity.Distributor;
import com.igomall.service.DistributionCommissionService;

/**
 * Service - 分销佣金
 * 
 */
@Service
public class DistributionCommissionServiceImpl extends BaseServiceImpl<DistributionCommission> implements DistributionCommissionService {

	@Inject
	private DistributionCommissionDao distributionCommissionDao;

	@Override
	@Transactional(readOnly = true)
	public Page<DistributionCommission> findPage(Distributor distributor, Pageable pageable) {
		IPage<DistributionCommission> iPage = getPluginsPage(pageable);
		iPage.setRecords(distributionCommissionDao.findPage(iPage, getPageable(pageable), distributor));
		return super.findPage(iPage, pageable);
	}

}