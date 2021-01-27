package com.igomall.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.igomall.Page;
import com.igomall.Pageable;
import com.igomall.dao.MemberDepositLogDao;
import com.igomall.entity.Member;
import com.igomall.entity.MemberDepositLog;
import com.igomall.service.MemberDepositLogService;

/**
 * Service - 会员预存款记录
 * 
 */
@Service
public class MemberDepositLogServiceImpl extends BaseServiceImpl<MemberDepositLog> implements MemberDepositLogService {

	@Inject
	private MemberDepositLogDao memberDepositLogDao;

	@Override
	@Transactional(readOnly = true)
	public Page<MemberDepositLog> findPage(Member member, Pageable pageable) {
		IPage<MemberDepositLog> iPage = getPluginsPage(pageable);
		iPage.setRecords(memberDepositLogDao.findPage(iPage, getPageable(pageable), member));
		return super.findPage(iPage, pageable);
	}

}