package com.igomall.service.impl;

import javax.inject.Inject;

import com.igomall.Page;
import com.igomall.Pageable;
import com.igomall.dao.StockLogDao;
import com.igomall.entity.StockLog;
import com.igomall.entity.Store;
import com.igomall.service.StockLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * Service - 库存记录
 * 
 */
@Service
public class StockLogServiceImpl extends BaseServiceImpl<StockLog> implements StockLogService {

	@Inject
	private StockLogDao stockLogDao;

	@Override
	@Transactional(readOnly = true)
	public Page<StockLog> findPage(Store store, Pageable pageable) {
		IPage<StockLog> iPage = getPluginsPage(pageable);
		iPage.setRecords(stockLogDao.findPage(iPage, getPageable(pageable), store));
		return super.findPage(iPage, pageable);
	}

}