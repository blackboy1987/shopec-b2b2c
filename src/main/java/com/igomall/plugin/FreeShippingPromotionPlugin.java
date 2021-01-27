package com.igomall.plugin;

import com.igomall.entity.FreeShippingAttribute;
import org.springframework.stereotype.Component;

import com.igomall.entity.Promotion;

/**
 * Plugin - 免运费
 * 
 */
@Component("freeShippingPromotionPlugin")
public class FreeShippingPromotionPlugin extends PromotionPlugin {

	@Override
	public String getName() {
		return "免运费";
	}

	@Override
	public String getInstallUrl() {
		return "/admin/plugin/free_shipping_promotion/install";
	}

	@Override
	public String getUninstallUrl() {
		return "/admin/plugin/free_shipping_promotion/uninstall";
	}

	@Override
	public String getSettingUrl() {
		return "/admin/plugin/free_shipping_promotion/setting";
	}

	@Override
	public String getAddUrl() {
		return "/business/free_shipping_promotion/add";
	}

	@Override
	public String getEditUrl() {
		return "/business/free_shipping_promotion/edit";
	}

	@Override
	public boolean isFreeShipping(Promotion promotion) {
		FreeShippingAttribute freeShippingAttribute = (FreeShippingAttribute) promotion.getPromotionDefaultAttribute();
		return freeShippingAttribute.getIsFreeShipping();
	}
	
}