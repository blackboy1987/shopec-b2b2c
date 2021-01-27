package com.igomall.dao;

import java.util.List;

import com.igomall.entity.StockLog;
import com.igomall.entity.Store;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * Dao - 库存记录
 * 
 */
public interface StockLogDao extends BaseDao<StockLog> {

	/**
	 * 查找库存记录分页
	 * 
	 * @param store
	 *            店铺
	 * @param pageable
	 *            分页
	 * @return 库存记录分页
	 */
	List<StockLog> findPage(IPage<StockLog> iPage, @Param("ew")QueryWrapper<StockLog> queryWrapper, @Param("store") Store store);

}