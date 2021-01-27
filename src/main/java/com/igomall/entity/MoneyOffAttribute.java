package com.igomall.entity;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.core.enums.IEnum;

/**
 * 满减折扣属性
 * 
 */
@Entity(name = "MoneyOffAttribute")
public class MoneyOffAttribute extends PromotionDefaultAttribute {

	private static final long serialVersionUID = 8274749416652276603L;

	/**
	 * 减免类型
	 */
	public enum DiscountType implements IEnum<Integer> {

		/**
		 * 固定价格
		 */
		FIX_PRICE(0),

		/**
		 * 金额减免
		 */
		AMOUNT_OFF(1),

		/**
		 * 百分比减免
		 */
		PERCENT_OFF(2),

		/**
		 * 重复金额减免
		 */
		DUPLICATE_AMOUNT_OFF(3);
		
		private int value;

		DiscountType(final int value) {
			this.value = value;
		}
		
		@Override
		public Integer getValue() {
			return this.value;
		}
	}

	/**
	 * 条件值
	 */
	@Min(0)
	@Digits(integer = 12, fraction = 3)
	private BigDecimal conditionValue;

	/**
	 * 减免类型
	 */
	@NotNull
	private MoneyOffAttribute.DiscountType discountType;

	/**
	 * 折扣值
	 */
	@NotNull
	@Min(0)
	@Digits(integer = 12, fraction = 3)
	private BigDecimal discounValue;

	/**
	 * 获取条件值
	 * 
	 * @return 条件值
	 */
	public BigDecimal getConditionValue() {
		return conditionValue;
	}

	/**
	 * 设置条件值
	 * 
	 * @param conditionValue
	 *            条件值
	 */
	public void setConditionValue(BigDecimal conditionValue) {
		this.conditionValue = conditionValue;
	}

	/**
	 * 获取折扣类型
	 * 
	 * @return 折扣类型
	 */
	public MoneyOffAttribute.DiscountType getDiscountType() {
		return discountType;
	}

	/**
	 * 设置折扣类型
	 * 
	 * @param discountType
	 *            折扣类型
	 */
	public void setDiscountType(MoneyOffAttribute.DiscountType discountType) {
		this.discountType = discountType;
	}

	/**
	 * 获取折扣值
	 * 
	 * @return 折扣值
	 */
	public BigDecimal getDiscounValue() {
		return discounValue;
	}

	/**
	 * 设置折扣值
	 * 
	 * @param discounValue
	 *            折扣值
	 */
	public void setDiscounValue(BigDecimal discounValue) {
		this.discounValue = discounValue;
	}
	
}
