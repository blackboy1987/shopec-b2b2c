package com.igomall.dao;

import com.igomall.entity.Cart;

/**
 * Dao - 购物车
 * 
 */
public interface CartDao extends BaseDao<Cart> {

	/**
	 * 删除过期购物车
	 */
	void deleteExpired();

}