package com.igomall.dao;

import java.util.List;

import com.igomall.entity.Store;
import com.igomall.entity.StoreRank;
import com.igomall.entity.Svc;
import org.apache.ibatis.annotations.Param;

/**
 * Dao - 服务
 * 
 */
public interface SvcDao extends BaseDao<Svc> {

	/**
	 * 查找服务
	 * 
	 * @param store
	 *            店铺
	 * @param promotionPluginId
	 *            促销插件Id
	 * @param storeRank
	 *            店铺等级
	 * @param orders
	 *            排序
	 * @return 服务
	 */
	List<Svc> findByStore(@Param("store") Store store, @Param("promotionPluginId")String promotionPluginId, @Param("storeRank") StoreRank storeRank);

}