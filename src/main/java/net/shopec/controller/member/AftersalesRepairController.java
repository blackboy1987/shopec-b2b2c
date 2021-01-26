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
import net.shopec.entity.AftersalesRepair;
import net.shopec.entity.Area;
import net.shopec.entity.Member;
import net.shopec.entity.Order;
import net.shopec.exception.UnauthorizedException;
import net.shopec.security.CurrentUser;
import net.shopec.service.AftersalesRepairService;
import net.shopec.service.AftersalesService;
import net.shopec.service.AreaService;
import net.shopec.service.OrderService;

/**
 * Controller - 维修
 * 
 */
@Controller("memberAftersalesRepairController")
@RequestMapping("/member/aftersales_repair")
public class AftersalesRepairController extends BaseController {

	@Inject
	private AftersalesRepairService aftersalesRepairService;
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
	public void populateModel(Long aftersalesRepairId, Long orderId, @CurrentUser Member currentUser, ModelMap model) {
		AftersalesRepair aftersalesRepair = aftersalesRepairService.find(aftersalesRepairId);
		if (aftersalesRepair != null && !currentUser.equals(aftersalesRepair.getMember())) {
			throw new UnauthorizedException();
		}
		Order order = orderService.find(orderId);
		if (order != null && !currentUser.equals(order.getMember())) {
			throw new UnauthorizedException();
		}
		model.addAttribute("aftersalesRepair", aftersalesRepair);
		model.addAttribute("order", order);
	}

	/**
	 * 维修
	 */
	@PostMapping("/repair")
	public ResponseEntity<?> repair(@ModelAttribute("aftersalesRepairForm") AftersalesRepair aftersalesRepairForm, @ModelAttribute(binding = false) Order order, Long areaId) {
		if (order == null) {
			return Results.UNPROCESSABLE_ENTITY;
		}

		aftersalesService.filterNotActiveAftersalesItem(aftersalesRepairForm);
		if (aftersalesService.existsIllegalAftersalesItems(aftersalesRepairForm.getAftersalesItems())) {
			return Results.UNPROCESSABLE_ENTITY;
		}

		Area area = areaService.find(areaId);
		aftersalesRepairForm.setStatus(Aftersales.Status.PENDING);
		aftersalesRepairForm.setArea(area);
		aftersalesRepairForm.setMember(order.getMember());
		aftersalesRepairForm.setStore(order.getStore());

		if (!isValid(aftersalesRepairForm)) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		aftersalesRepairService.save(aftersalesRepairForm);
		return Results.OK;
	}

}