package net.shopec.entity;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * 免运费属性
 * 
 */
@Entity(name = "FreeShippingAttribute")
public class FreeShippingAttribute extends PromotionDefaultAttribute {

	private static final long serialVersionUID = -1343559914106941832L;

	/**
	 * 是否免运费
	 */
	@NotNull
	private Boolean isFreeShipping;

	/**
	 * 获取是否免运费
	 * 
	 * @return 是否免运费
	 */
	public Boolean getIsFreeShipping() {
		return isFreeShipping;
	}

	/**
	 * 设置是否免运费
	 * 
	 * @param isFreeShipping
	 *            是否免运费
	 */
	public void setIsFreeShipping(Boolean isFreeShipping) {
		this.isFreeShipping = isFreeShipping;
	}
	
}
