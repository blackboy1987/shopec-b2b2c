package net.shopec.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;

/**
 * 优惠券属性
 * 
 */
@Entity(name = "CouponAttribute")
public class CouponAttribute extends PromotionDefaultAttribute {

	private static final long serialVersionUID = -616862055443133271L;

	/**
	 * 优惠券
	 */
	private Set<Coupon> coupons = new HashSet<>();

	/**
	 * 获取优惠券
	 * 
	 * @return 优惠券
	 */
	public Set<Coupon> getCoupons() {
		return coupons;
	}

	/**
	 * 设置优惠券
	 * 
	 * @param coupons
	 *            优惠券
	 */
	public void setCoupons(Set<Coupon> coupons) {
		this.coupons = coupons;
	}
	
}
