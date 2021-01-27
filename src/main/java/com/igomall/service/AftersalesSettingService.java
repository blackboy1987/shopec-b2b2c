package com.igomall.service;

import com.igomall.entity.AftersalesSetting;
import com.igomall.entity.Store;

/**
 * Service - 售后设置
 * 
 */
public interface AftersalesSettingService extends BaseService<AftersalesSetting> {

	/**
	 * 通过店铺查找售后设置
	 * 
	 * @param store
	 *            店铺
	 * @return 售后设置
	 */
	AftersalesSetting findByStore(Store store);

}