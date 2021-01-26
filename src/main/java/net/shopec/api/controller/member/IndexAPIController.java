package net.shopec.api.controller.member;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.shopec.api.model.ApiResult;
import net.shopec.api.util.ResultUtils;
import net.shopec.entity.Member;
import net.shopec.entity.MemberRank;
import net.shopec.entity.Order;
import net.shopec.security.CurrentUser;
import net.shopec.service.ConsultationService;
import net.shopec.service.CouponCodeService;
import net.shopec.service.MessageService;
import net.shopec.service.OrderService;
import net.shopec.service.ProductFavoriteService;
import net.shopec.service.ProductNotifyService;
import net.shopec.service.ReviewService;

/**
 * 首页 - 接口类
 */
@RestController("memberApiIndexController")
@RequestMapping("/api/member/index")
public class IndexAPIController extends BaseAPIController {
	
	/**
	 * 最新订单数量
	 */
	private static final int NEW_ORDER_SIZE = 3;

	@Inject
	private OrderService orderService;
	@Inject
	private CouponCodeService couponCodeService;
	@Inject
	private MessageService messageService;
	@Inject
	private ProductFavoriteService productFavoriteService;
	@Inject
	private ProductNotifyService productNotifyService;
	@Inject
	private ReviewService reviewService;
	@Inject
	private ConsultationService consultationService;
	
	/**
	 * 首页
	 */
	@GetMapping
	public ApiResult index(@CurrentUser Member currentUser) {
		
		Map<String, Object> data = new HashMap<>();
		data.put("pendingPaymentOrderCount", orderService.count(null, Order.Status.PENDING_PAYMENT, null, currentUser, null, null, null, null, null, null, false));
		data.put("pendingShipmentOrderCount", orderService.count(null, Order.Status.PENDING_SHIPMENT, null, currentUser, null, null, null, null, null, null, false));
		data.put("shippedOrderCount", orderService.count(null, Order.Status.SHIPPED, null, currentUser, null, null, null, null, null, null, null));
		data.put("unreadMessageCount", messageService.unreadMessageCount(null, currentUser));
		data.put("couponCodeCount", couponCodeService.count(null, currentUser, null, false, false));
		data.put("productFavoriteCount", productFavoriteService.count(currentUser));
		data.put("productNotifyCount", productNotifyService.count(currentUser, null, null, null));
		data.put("reviewCount", reviewService.count(currentUser, null, null, null));
		data.put("consultationCount", consultationService.count(currentUser, null, null));
		data.put("newOrders", orderService.findList(null, null, null, currentUser, null, null, null, null, null, null, null, NEW_ORDER_SIZE, null, null).size());
		
		Map<String, Object> member = new HashMap<>();
		member.put("username", currentUser.getUsername());
		MemberRank memberRank = currentUser.getMemberRank();
		if (memberRank != null) {
			member.put("memberRankName", memberRank.getName());
		}
		member.put("balance", currency(currentUser.getBalance(), true, true));
		if (currentUser.getFrozenAmount().compareTo(BigDecimal.ZERO) > 0) {
			member.put("frozenAmount", currency(currentUser.getFrozenAmount(), true, true));
		}
		member.put("amount", currency(currentUser.getBalance(), true, true));
		member.put("point", currentUser.getPoint());
		data.put("member", member);
		return ResultUtils.ok(data);
	}

}
