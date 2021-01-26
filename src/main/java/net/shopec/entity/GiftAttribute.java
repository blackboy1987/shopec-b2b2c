package net.shopec.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;

/**
 * 赠品属性
 * 
 */
@Entity(name = "GiftAttribute")
public class GiftAttribute extends PromotionDefaultAttribute {

	private static final long serialVersionUID = -1996945993194404912L;

	/**
	 * 赠品
	 */
	private Set<Sku> gifts = new HashSet<>();

	/**
	 * 获取赠品
	 * 
	 * @return 赠品
	 */
	public Set<Sku> getGifts() {
		return gifts;
	}

	/**
	 * 设置赠品
	 * 
	 * @param gifts
	 *            赠品
	 */
	public void setGifts(Set<Sku> gifts) {
		this.gifts = gifts;
	}
	
}
