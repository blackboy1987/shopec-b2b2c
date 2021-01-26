package net.shopec.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;

import net.shopec.Page;
import net.shopec.Pageable;
import net.shopec.dao.PointLogDao;
import net.shopec.entity.Member;
import net.shopec.entity.PointLog;
import net.shopec.service.PointLogService;

/**
 * Service - 积分记录
 * 
 */
@Service
public class PointLogServiceImpl extends BaseServiceImpl<PointLog> implements PointLogService {

	@Inject
	private PointLogDao pointLogDao;

	@Override
	@Transactional(readOnly = true)
	public Page<PointLog> findPage(Member member, Pageable pageable) {
		IPage<PointLog> iPage = getPluginsPage(pageable);
		iPage.setRecords(pointLogDao.findPage(iPage, getPageable(pageable), member));
		return super.findPage(iPage, pageable);
	}

}