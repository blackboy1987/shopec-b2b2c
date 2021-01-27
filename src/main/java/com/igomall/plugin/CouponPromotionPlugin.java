package com.igomall.plugin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.igomall.entity.Coupon;
import com.igomall.entity.CouponAttribute;
import com.igomall.entity.Promotion;
import com.igomall.entity.Store;

/**
 * Plugin - 优惠券
 * 
 */
@Component("couponPromotionPlugin")
public class CouponPromotionPlugin extends PromotionPlugin {

	@Override
	public String getName() {
		return "优惠券";
	}

	@Override
	public String getInstallUrl() {
		return "/admin/plugin/coupon_promotion/install";
	}

	@Override
	public String getUninstallUrl() {
		return "/admin/plugin/coupon_promotion/uninstall";
	}

	@Override
	public String getSettingUrl() {
		return "/admin/plugin/coupon_promotion/setting";
	}

	@Override
	public String getAddUrl() {
		return "/business/coupon_promotion/add";
	}

	@Override
	public String getEditUrl() {
		return "/business/coupon_promotion/edit";
	}

	@Override
	public List<Coupon> getCoupons(Promotion promotion, Store store) {
		CouponAttribute couponAttribute = (CouponAttribute) promotion.getPromotionDefaultAttribute();
		return new ArrayList<>(couponAttribute.getCoupons());
	}

}