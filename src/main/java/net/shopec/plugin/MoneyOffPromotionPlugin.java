package net.shopec.plugin;

import java.math.BigDecimal;
import java.math.MathContext;

import org.springframework.stereotype.Component;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import net.shopec.entity.MoneyOffAttribute;
import net.shopec.entity.Promotion;

/**
 * Plugin - 满减折扣
 * 
 */
@Component("moneyOffPromotionPlugin")
public class MoneyOffPromotionPlugin extends PromotionPlugin {

	/**
	 * 重复金额减免表达式
	 */
	public static final String DUPLICATE_AMOUNT_OFF_EXPRESSION = "price-((price/%s) as int) * %s";

	@Override
	public String getName() {
		return "满减折扣";
	}

	@Override
	public String getInstallUrl() {
		return "/admin/plugin/money_off_promotion/install";
	}

	@Override
	public String getUninstallUrl() {
		return "/admin/plugin/money_off_promotion/uninstall";
	}

	@Override
	public String getSettingUrl() {
		return "/admin/plugin/money_off_promotion/setting";
	}

	@Override
	public String getAddUrl() {
		return "/business/money_off_promotion/add";
	}

	@Override
	public String getEditUrl() {
		return "/business/money_off_promotion/edit";
	}

	@Override
	public BigDecimal computeAdjustmentValue(Promotion promotion, BigDecimal price, int quantity) {
		if (promotion != null && price != null && price.compareTo(BigDecimal.ZERO) > 0 && quantity > 0) {
			BigDecimal result = BigDecimal.ZERO;
			MoneyOffAttribute moneyOffAttribute = (MoneyOffAttribute) promotion.getPromotionDefaultAttribute();
			BigDecimal discounValue = moneyOffAttribute.getDiscounValue();
			BigDecimal conditionValue = moneyOffAttribute.getConditionValue();
			if (moneyOffAttribute.getDiscountType() == null || discounValue == null || discounValue.compareTo(BigDecimal.ZERO) <= 0) {
				return price;
			}
			switch (moneyOffAttribute.getDiscountType()) {
			case FIX_PRICE:
				result = discounValue;
				break;
			case AMOUNT_OFF:
				result = price.subtract(discounValue);
				break;
			case PERCENT_OFF:
				result = price.multiply(discounValue);
				break;
			case DUPLICATE_AMOUNT_OFF:
				if (conditionValue == null || conditionValue.compareTo(BigDecimal.ZERO) <= 0) {
					return price;
				}
				try {
					Binding binding = new Binding();
					binding.setVariable("price", price);
					GroovyShell groovyShell = new GroovyShell(binding);
					result = new BigDecimal(String.valueOf(groovyShell.evaluate(String.format(DUPLICATE_AMOUNT_OFF_EXPRESSION, conditionValue, discounValue))), MathContext.DECIMAL32);
				} catch (Exception e) {
					return price;
				}
				break;
			}
			if (result.compareTo(price) > 0) {
				return price;
			}
			return result.compareTo(BigDecimal.ZERO) > 0 ? result : BigDecimal.ZERO;
		}
		return price;
	}

}