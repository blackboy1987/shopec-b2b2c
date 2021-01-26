package net.shopec.controller.member;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import net.shopec.Results;
import net.shopec.entity.Aftersales;
import net.shopec.entity.AftersalesReplacement;
import net.shopec.entity.Area;
import net.shopec.entity.Member;
import net.shopec.entity.Order;
import net.shopec.exception.UnauthorizedException;
import net.shopec.security.CurrentUser;
import net.shopec.service.AftersalesReplacementService;
import net.shopec.service.AftersalesService;
import net.shopec.service.AreaService;
import net.shopec.service.OrderService;

/**
 * Controller - 换货
 * 
 */
@Controller("memberAftersalesReplacementController")
@RequestMapping("/member/aftersales_replacement")
public class AftersalesReplacementController extends BaseController {

	@Inject
	private AftersalesReplacementService aftersalesReplacementService;
	@Inject
	private OrderService orderService;
	@Inject
	private AftersalesService aftersalesService;
	@Inject
	private AreaService areaService;

	/**
	 * 添加属性
	 */
	@ModelAttribute
	public void populateModel(Long aftersalesReplacementId, Long orderId, @CurrentUser Member currentUser, ModelMap model) {
		AftersalesReplacement aftersalesReplacement = aftersalesReplacementService.find(aftersalesReplacementId);
		if (aftersalesReplacement != null && !currentUser.equals(aftersalesReplacement.getMember())) {
			throw new UnauthorizedException();
		}
		Order order = orderService.find(orderId);
		if (order != null && !currentUser.equals(order.getMember())) {
			throw new UnauthorizedException();
		}
		model.addAttribute("aftersalesReplacement", aftersalesReplacement);
		model.addAttribute("order", order);
	}

	/**
	 * 换货
	 */
	@PostMapping("/replacement")
	public ResponseEntity<?> replacement(@ModelAttribute("aftersalesReplacementForm") AftersalesReplacement aftersalesReplacementForm, @ModelAttribute(binding = false) Order order, Long areaId) {
		if (order == null) {
			return Results.UNPROCESSABLE_ENTITY;
		}

		aftersalesService.filterNotActiveAftersalesItem(aftersalesReplacementForm);
		if (aftersalesService.existsIllegalAftersalesItems(aftersalesReplacementForm.getAftersalesItems())) {
			return Results.UNPROCESSABLE_ENTITY;
		}

		Area area = areaService.find(areaId);
		aftersalesReplacementForm.setStatus(Aftersales.Status.PENDING);
		aftersalesReplacementForm.setMember(order.getMember());
		aftersalesReplacementForm.setStore(order.getStore());
		aftersalesReplacementForm.setArea(area);

		if (!isValid(aftersalesReplacementForm)) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		aftersalesReplacementService.save(aftersalesReplacementForm);
		return Results.OK;
	}

}