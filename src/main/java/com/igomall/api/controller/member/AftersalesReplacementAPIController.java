package com.igomall.api.controller.member;

import javax.inject.Inject;

import com.igomall.service.AftersalesReplacementService;
import com.igomall.service.AftersalesService;
import com.igomall.service.AreaService;
import com.igomall.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.igomall.api.model.ApiResult;
import com.igomall.api.util.ResultUtils;
import com.igomall.entity.Aftersales;
import com.igomall.entity.AftersalesReplacement;
import com.igomall.entity.Area;
import com.igomall.entity.Member;
import com.igomall.entity.Order;

/**
 * 换货 - 接口类
 */
@RestController("memberApiAftersalesReplacementController")
@RequestMapping("/api/member/aftersales_replacement")
public class AftersalesReplacementAPIController extends BaseAPIController {

	@Inject
	private AftersalesReplacementService aftersalesReplacementService;
	@Inject
	private OrderService orderService;
	@Inject
	private AftersalesService aftersalesService;
	@Inject
	private AreaService areaService;

	/**
	 * 换货
	 */
	@PostMapping("/replacement")
	public ApiResult replacement(@RequestBody AftersalesReplacement aftersalesReplacementForm, @RequestParam("orderId") Long orderId, @RequestParam("areaId") Long areaId) {
		
		Order order = orderService.find(orderId);
		if (order == null) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		Member currentUser = getCurrent();
		if (order != null && !currentUser.equals(order.getMember())) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		
		aftersalesService.filterNotActiveAftersalesItem(aftersalesReplacementForm);
		if (aftersalesService.existsIllegalAftersalesItems(aftersalesReplacementForm.getAftersalesItems())) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}

		Area area = areaService.find(areaId);
		aftersalesReplacementForm.setStatus(Aftersales.Status.PENDING);
		aftersalesReplacementForm.setMember(order.getMember());
		aftersalesReplacementForm.setStore(order.getStore());
		aftersalesReplacementForm.setArea(area);

		if (!isValid(aftersalesReplacementForm)) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		aftersalesReplacementService.save(aftersalesReplacementForm);
		return ResultUtils.ok();
	}

}