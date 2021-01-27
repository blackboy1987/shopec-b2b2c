package com.igomall.service.impl;

import javax.inject.Inject;

import com.igomall.Page;
import com.igomall.Pageable;
import com.igomall.dao.PointLogDao;
import com.igomall.entity.Member;
import com.igomall.entity.PointLog;
import com.igomall.service.PointLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;

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