package com.igomall.api.controller.member;

import javax.inject.Inject;

import com.igomall.entity.Aftersales;
import com.igomall.entity.AftersalesReturns;
import com.igomall.entity.Member;
import com.igomall.entity.Order;
import com.igomall.service.AftersalesReturnsService;
import com.igomall.service.AftersalesService;
import com.igomall.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.igomall.api.model.ApiResult;
import com.igomall.api.util.ResultUtils;


/**
 * 退货 - 接口类
 */
@RestController("memberApiAftersalesReturnsController")
@RequestMapping("/api/member/aftersales_returns")
public class AftersalesReturnsAPIController extends BaseAPIController {

	@Inject
	private AftersalesReturnsService aftersalesReturnsService;
	@Inject
	private OrderService orderService;
	@Inject
	private AftersalesService aftersalesService;

	/**
	 * 退货
	 */
	@PostMapping("/returns")
	public ApiResult returns(@RequestBody AftersalesReturns aftersalesReturnsForm, @RequestParam("orderId") Long orderId) {
		Order order = orderService.find(orderId);
		if (order == null) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}

		Member currentUser = getCurrent();
		if (order != null && !currentUser.equals(order.getMember())) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		
		aftersalesService.filterNotActiveAftersalesItem(aftersalesReturnsForm);
		if (aftersalesService.existsIllegalAftersalesItems(aftersalesReturnsForm.getAftersalesItems())) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}

		aftersalesReturnsForm.setStatus(Aftersales.Status.PENDING);
		aftersalesReturnsForm.setMember(order.getMember());
		aftersalesReturnsForm.setStore(order.getStore());

		if (!isValid(aftersalesReturnsForm)) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		aftersalesReturnsService.save(aftersalesReturnsForm);
		return ResultUtils.ok();
	}

}