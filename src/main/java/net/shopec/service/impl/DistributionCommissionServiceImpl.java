package net.shopec.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;

import net.shopec.Page;
import net.shopec.Pageable;
import net.shopec.dao.DistributionCommissionDao;
import net.shopec.entity.DistributionCommission;
import net.shopec.entity.Distributor;
import net.shopec.service.DistributionCommissionService;

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