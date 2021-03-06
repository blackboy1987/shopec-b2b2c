package com.igomall.dao;

import java.math.BigDecimal;
import java.util.Collection;

import com.igomall.entity.PaymentTransaction;
import com.igomall.plugin.PaymentPlugin;
import org.apache.ibatis.annotations.Param;

/**
 * Dao - 支付事务
 * 
 */
public interface PaymentTransactionDao extends BaseDao<PaymentTransaction> {

	/**
	 * 查找可用支付事务
	 * 
	 * @param lineItem
	 *            支付明细
	 * @param paymentPlugin
	 *            支付插件
	 * @return 可用支付事务，若不存在则返回null
	 */
	PaymentTransaction findAvailable(@Param("lineItem")PaymentTransaction.LineItem lineItem, @Param("paymentPlugin") PaymentPlugin paymentPlugin, @Param("amount")BigDecimal amount, @Param("fee")BigDecimal fee);

	/**
	 * 查找可用父支付事务
	 * 
	 * @param lineItems
	 *            支付明细
	 * @param paymentPlugin
	 *            支付插件
	 * @return 可用父支付事务，若不存在则返回null
	 */
	PaymentTransaction findAvailableParent(Collection<PaymentTransaction.LineItem> lineItems, PaymentPlugin paymentPlugin);

}