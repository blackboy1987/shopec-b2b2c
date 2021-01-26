package net.shopec.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;

import net.shopec.Page;
import net.shopec.Pageable;
import net.shopec.dao.StockLogDao;
import net.shopec.entity.StockLog;
import net.shopec.entity.Store;
import net.shopec.service.StockLogService;

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