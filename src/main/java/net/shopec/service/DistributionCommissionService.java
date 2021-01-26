package net.shopec.service;

import net.shopec.Page;
import net.shopec.Pageable;
import net.shopec.entity.DistributionCommission;
import net.shopec.entity.Distributor;

/**
 * Service - 分销佣金
 * 
 */
public interface DistributionCommissionService extends BaseService<DistributionCommission> {

	/**
	 * 查找分销佣金记录分页
	 * 
	 * @param distributor
	 *            分销员
	 * @param pageable
	 *            分页信息
	 * @return 分销佣金记录分页
	 */
	Page<DistributionCommission> findPage(Distributor distributor, Pageable pageable);

}