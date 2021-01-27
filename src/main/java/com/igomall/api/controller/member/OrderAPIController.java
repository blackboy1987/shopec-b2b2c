package com.igomall.api.controller.member;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.igomall.config.WxPayConfiguration;
import com.igomall.plugin.PaymentPlugin;
import com.igomall.util.SpringUtils;
import com.igomall.util.SystemUtils;
import com.igomall.util.WebUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.BaseWxPayRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.service.WxPayService;

import com.igomall.CommonAttributes;
import com.igomall.Page;
import com.igomall.Pageable;
import com.igomall.Setting;
import com.igomall.api.model.ApiResult;
import com.igomall.api.util.ResultUtils;
import com.igomall.entity.Cart;
import com.igomall.entity.CartItem;
import com.igomall.entity.CouponCode;
import com.igomall.entity.Invoice;
import com.igomall.entity.Member;
import com.igomall.entity.Order;
import com.igomall.entity.OrderItem;
import com.igomall.entity.OrderShipping;
import com.igomall.entity.PaymentItem;
import com.igomall.entity.PaymentMethod;
import com.igomall.entity.PaymentTransaction;
import com.igomall.entity.Product;
import com.igomall.entity.Receiver;
import com.igomall.entity.ShippingMethod;
import com.igomall.entity.Sku;
import com.igomall.security.CurrentCart;
import com.igomall.security.CurrentUser;
import com.igomall.service.AreaService;
import com.igomall.service.CouponCodeService;
import com.igomall.service.OrderService;
import com.igomall.service.PaymentMethodService;
import com.igomall.service.PaymentTransactionService;
import com.igomall.service.PluginService;
import com.igomall.service.ReceiverService;
import com.igomall.service.ShippingMethodService;
import com.igomall.service.SkuService;
import com.igomall.service.UserService;

/**
 * 订单 - 接口类
 */
@RestController
@RequestMapping("/api/member/order")
public class OrderAPIController extends BaseAPIController {

	private static final Logger _logger = LoggerFactory.getLogger(OrderAPIController.class);
	
	@Inject
	private SkuService skuService;
	@Inject
	private AreaService areaService;
	@Inject
	private ReceiverService receiverService;
	@Inject
	private PaymentMethodService paymentMethodService;
	@Inject
	private ShippingMethodService shippingMethodService;
	@Inject
	private UserService userService;
	@Inject
	private OrderService orderService;
	@Inject
	private CouponCodeService couponCodeService;
	@Inject
	private PaymentTransactionService paymentTransactionService;
	@Inject
	private PluginService pluginService;
	@Inject
    private WxPayService wxService;
	
	/**
	 * 检查购物车
	 */
	@GetMapping("/check_cart")
	public ApiResult checkCart(@CurrentCart Cart currentCart) {
		if (currentCart == null || currentCart.isEmpty()) {
			return ResultUtils.unprocessableEntity("shop.order.cartEmpty");
		}
		if (currentCart.hasNotActive()) {
			return ResultUtils.unprocessableEntity("shop.order.cartHasNotActive");
		}
		if (currentCart.hasNotMarketable()) {
			return ResultUtils.unprocessableEntity("shop.order.cartHasNotMarketable");
		}
		if (currentCart.hasLowStock()) {
			return ResultUtils.unprocessableEntity("shop.order.cartHasLowStock");
		}
		if (currentCart.hasExpiredProduct()) {
			return ResultUtils.unprocessableEntity("shop.order.cartHasExpiredProduct");
		}
		return ResultUtils.ok(true);
	}
	
	/**
	 * 收货地址列表
	 */
	@GetMapping("/receiver_list")
	public ApiResult receiverList() {
		Member currentUser = userService.getCurrent(Member.class);
		List<Map<String, Object>> map = new ArrayList<>();
		for (Receiver receiver : receiverService.findList(currentUser)) {
			Map<String, Object> item = new HashMap<>();
			item.put("id", receiver.getId());
			item.put("consignee", receiver.getConsignee());
			item.put("areaName", receiver.getAreaName());
			item.put("address", receiver.getAddress());
			item.put("isDefault", receiver.getIsDefault());
			item.put("phone", receiver.getPhone());
			item.put("areaId", receiver.getArea().getId());
			map.add(item);
		}
		return ResultUtils.ok(map);
	}
	
	/**
	 * 添加收货地址
	 */
	@PostMapping("/add_receiver")
	public ApiResult addReceiver(@RequestBody Receiver receiver, @RequestParam("areaId") Long areaId) {
		
		Member currentUser = userService.getCurrent(Member.class);
		receiver.setArea(areaService.find(areaId));
		if (!isValid(receiver)) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		if (Receiver.MAX_RECEIVER_COUNT != null && currentUser.getReceivers().size() >= Receiver.MAX_RECEIVER_COUNT) {
			return ResultUtils.unprocessableEntity("shop.order.addReceiverCountNotAllowed", Receiver.MAX_RECEIVER_COUNT);
		}
		receiver.setAreaName(null);
		receiver.setMember(currentUser);
		return ResultUtils.ok(receiverService.save(receiver));
	}
	
	/**
	 * 结算
	 */
	@GetMapping("/checkout")
	public ApiResult checkout(Long skuId, Integer quantity, @CurrentUser Member currentUser, @CurrentCart Cart currentCart) {
		Cart cart;
		Order.Type orderType;
		if (skuId != null) {
			Sku sku = skuService.find(skuId);
			if (sku == null) {
				return ResultUtils.UNPROCESSABLE_ENTITY;
			}
			if (Product.Type.GIFT.equals(sku.getType())) {
				return ResultUtils.UNPROCESSABLE_ENTITY;
			}
			if (quantity == null || quantity < 1) {
				return ResultUtils.UNPROCESSABLE_ENTITY;
			}

			cart = generateCart(currentUser, sku, quantity);

			switch (sku.getType()) {
			case GENERAL:
				orderType = Order.Type.GENERAL;
				break;
			case EXCHANGE:
				orderType = Order.Type.EXCHANGE;
				break;
			default:
				orderType = null;
				break;
			}
		} else {
			cart = currentCart;
			orderType = Order.Type.GENERAL;
		}
		if (cart == null || cart.isEmpty()) {
			return ResultUtils.unprocessableEntity("shop.order.cartEmpty");
		}
		if (cart.hasNotActive()) {
			return ResultUtils.unprocessableEntity("shop.order.cartHasNotActive");
		}
		if (cart.hasNotMarketable()) {
			return ResultUtils.unprocessableEntity("shop.order.cartHasNotMarketable");
		}
		if (cart.hasLowStock()) {
			return ResultUtils.unprocessableEntity("shop.order.cartHasLowStock");
		}
		if (cart.hasExpiredProduct()) {
			return ResultUtils.unprocessableEntity("shop.order.cartHasExpiredProduct");
		}
		if (orderType == null) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}

		Receiver defaultReceiver = receiverService.findDefault(currentUser);
		List<Order> orders = orderService.generate(orderType, cart, defaultReceiver, null, null, null, null, null, null);

		Map<String, Object> data = new HashMap<>();
		
		// 收货地址
		Map<String, Object> receiver = new HashMap<>();
		if (defaultReceiver != null) {
			receiver.put("id", String.valueOf(defaultReceiver.getId()));
			receiver.put("consignee", defaultReceiver.getConsignee());
			receiver.put("phone", defaultReceiver.getPhone());
			receiver.put("areaName", defaultReceiver.getAreaName());
			receiver.put("address", defaultReceiver.getAddress());
			receiver.put("zipCode", defaultReceiver.getZipCode());
			data.put("receiver", receiver);
		}
		data.put("existReceiver", MapUtils.isEmpty(receiver) ? false : true);
		
		// 订单
		if (CollectionUtils.isNotEmpty(orders)) {
			Order order = orders.get(0);
			data.put("price", order.getPrice());
			data.put("fee", order.getFee());
			data.put("freight", order.getFreight());
			data.put("tax", order.getTax());
			data.put("promotionDiscount", order.getPromotionDiscount());
			data.put("couponDiscount", order.getCouponDiscount());
			data.put("amount", order.getAmount());
			data.put("amountPayable", order.getAmountPayable());
			data.put("exchangePoint", order.getExchangePoint());
			data.put("quantity", order.getQuantity());
			
			Setting setting = SystemUtils.getSetting();
			List<Map<String, Object>> orderItems = new ArrayList<>();
			for (OrderItem orderItem : order.getOrderItems()) {
				Product product = orderItem.getSku().getProduct();
				Map<String, Object> item = new HashMap<>();
				item.put("orderItemName", orderItem.getName());
				item.put("thumbnail", StringUtils.isEmpty(orderItem.getThumbnail()) ? setting.getSiteUrl() + setting.getDefaultThumbnailProductImage() : setting.getSiteUrl() + orderItem.getThumbnail());
				item.put("specifications", orderItem.getSpecifications());
				item.put("price", orderItem.getPrice());
				item.put("quantity", orderItem.getQuantity());
				item.put("subtotal", orderItem.getSubtotal());
				item.put("productId", product.getId());
				orderItems.add(item);
			}
			data.put("orderItems", orderItems);
		}
		
		return ResultUtils.ok(data);
	}
	
	/**
	 * 创建
	 */
	@PostMapping("/create")
	public ApiResult create(Long skuId, Integer quantity, String code, String invoiceTitle, String invoiceTaxNumber, BigDecimal balance, String memo, @CurrentUser Member currentUser, @CurrentCart Cart currentCart) {
		Cart cart;
		Order.Type orderType;
		if (skuId != null) {
			Sku sku = skuService.find(skuId);
			if (sku == null) {
				return ResultUtils.UNPROCESSABLE_ENTITY;
			}
			if (Product.Type.GIFT.equals(sku.getType())) {
				return ResultUtils.UNPROCESSABLE_ENTITY;
			}
			if (quantity == null || quantity < 1) {
				return ResultUtils.UNPROCESSABLE_ENTITY;
			}

			cart = generateCart(currentUser, sku, quantity);

			switch (sku.getType()) {
			case GENERAL:
				orderType = Order.Type.GENERAL;
				break;
			case EXCHANGE:
				orderType = Order.Type.EXCHANGE;
				break;
			default:
				orderType = null;
				break;
			}
		} else {
			cart = currentCart;
			orderType = Order.Type.GENERAL;
		}
		if (cart == null || cart.isEmpty()) {
			return ResultUtils.unprocessableEntity("shop.order.cartEmpty");
		}
		if (cart.hasNotActive()) {
			return ResultUtils.unprocessableEntity("shop.order.cartHasNotActive");
		}
		if (cart.hasNotMarketable()) {
			return ResultUtils.unprocessableEntity("shop.order.cartHasNotMarketable");
		}
		if (cart.hasLowStock()) {
			return ResultUtils.unprocessableEntity("shop.order.cartHasLowStock");
		}
		if (cart.hasExpiredProduct()) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		if (orderType == null) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		Receiver receiver = cart.getIsDelivery() ? receiverService.findDefault(currentUser) : null;
		if (cart.getIsDelivery() && (receiver == null || !currentUser.equals(receiver.getMember()))) {
			return ResultUtils.unprocessableEntity("member.receiver.add");
		}
		if (balance != null && balance.compareTo(BigDecimal.ZERO) < 0) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		if (balance != null && balance.compareTo(currentUser.getAvailableBalance()) > 0) {
			return ResultUtils.unprocessableEntity("shop.order.insufficientBalance");
		}
		if (currentUser.getPoint() < cart.getExchangePoint()) {
			return ResultUtils.unprocessableEntity("shop.order.lowPoint");
		}
		CouponCode couponCode = couponCodeService.findByCode(code);
		if (couponCode != null && couponCode.getCoupon() != null && !cart.isValid(couponCode.getCoupon().getStore(), couponCode)) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		PaymentMethod paymentMethod = paymentMethodService.find(1L);
		ShippingMethod shippingMethod = shippingMethodService.find(1L);
		Invoice invoice = StringUtils.isNotEmpty(invoiceTitle) ? new Invoice(invoiceTitle, invoiceTaxNumber, null) : null;
		List<Order> orders = orderService.create(orderType, cart, receiver, paymentMethod, shippingMethod, couponCode, invoice, balance, memo);
		if (CollectionUtils.isEmpty(orders)) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		Order order = orders.get(0);
		String orderSn = order.getSn();
		if (order.getType().equals(Order.Type.EXCHANGE)) {
			return ResultUtils.ok();
		}
		return getPay(orderSn, currentUser);
	}

	/**
	 * 支付
	 */
	@PostMapping("/payment")
	public ApiResult payment(@CurrentUser Member currentUser, String orderSn) {
		if (StringUtils.isEmpty(orderSn)) {
			return ResultUtils.unprocessableEntity("订单号不能为空!");
		}

		Order order = orderService.findBySn(orderSn);
		if (order == null) {
			return ResultUtils.unprocessableEntity("订单为空!");
		}
		BigDecimal amountPayable = order.getAmountPayable();
		if (order.getAmount().compareTo(order.getAmountPaid()) <= 0 || amountPayable.compareTo(BigDecimal.ZERO) <= 0) {
			return ResultUtils.unprocessableEntity("订单过期或应付金额小于0");
		}
		
		return getPay(orderSn, currentUser);
	}
	
	/**
	 * 生成购物车
	 * 
	 * @param member
	 *            会员
	 * @param sku
	 *            SKU
	 * @param quantity
	 *            数量
	 * @return 购物车
	 */
	public Cart generateCart(Member member, Sku sku, Integer quantity) {
		Assert.notNull(member, "[Assertion failed] - member is required; it must not be null");
		Assert.notNull(sku, "[Assertion failed] - sku is required; it must not be null");
		Assert.state(!Product.Type.GIFT.equals(sku.getType()), "[Assertion failed] - sku type can't be GIFT");
		Assert.notNull(quantity, "[Assertion failed] - quantity is required; it must not be null");
		Assert.state(quantity > 0, "[Assertion failed] - quantity must be greater than 0");

		Cart cart = new Cart();
		Set<CartItem> cartItems = new HashSet<>();
		CartItem cartItem = new CartItem();
		cartItem.setSku(sku);
		cartItem.setQuantity(quantity);
		cartItems.add(cartItem);
		cartItem.setCart(cart);
		cart.setMember(member);
		cart.setCartItems(cartItems);
		return cart;
	}

	/**
	 * 列表
	 */
	@GetMapping(path = "/list")
	public ApiResult list(@CurrentUser Member currentUser, Order.Status status, Boolean hasExpired) {
		
		Page<Order> pages = orderService.findPage(null, status, null, currentUser, null, null, null, null, null, null, hasExpired, new Pageable());
		
		Map<String, Object> data = new HashMap<>();
		Setting setting = SystemUtils.getSetting();
		
		// 订单头
		List<Map<String, Object>> list = new ArrayList<>();
		for (Order pOrder : pages.getContent()) {
			Map<String, Object> order = new HashMap<>();
			// 状态
			Map<String, Object> pStatus = new HashMap<>();
			pStatus.put("value", pOrder.getStatus().ordinal());
			pStatus.put("text", SpringUtils.getMessage("Order.Status." + pOrder.getStatus().name()));
			order.put("status", pStatus);
			order.put("sn", pOrder.getSn());
			order.put("amount", pOrder.getAmount());
			order.put("createdDate", pOrder.getCreatedDate());
			order.put("quantity", pOrder.getQuantity());
			list.add(order);
			
			// 订单行
			List<Map<String, Object>> items = new ArrayList<>();
			for (OrderItem orderItem : pOrder.getOrderItems()) {
				Map<String, Object> item = new HashMap<>();
				item.put("thumbnail", StringUtils.isEmpty(orderItem.getThumbnail()) ? setting.getSiteUrl() + setting.getDefaultThumbnailProductImage() : setting.getSiteUrl() + orderItem.getThumbnail());
				item.put("price", orderItem.getPrice());
				item.put("quantity", orderItem.getQuantity());
				items.add(item);
			}
			order.put("items", items);
		}
		data.put("list", list);
		return ResultUtils.ok(data);
	}
	
	
	/**
	 * 查看
	 */
	@GetMapping("/view")
	public ApiResult view(@CurrentUser Member currentUser, @RequestParam("orderSn") String orderSn) {
		Map<String, Object> data = new HashMap<>();
		Order pOrder = orderService.findBySn(orderSn);
		if (pOrder == null) {
			return ResultUtils.badRequest("订单不存在");
		}
		if (pOrder != null && !currentUser.equals(pOrder.getMember())) {
			return ResultUtils.badRequest("订单用户与当前用户不一致");
		}
		
		Map<String, Object> order = new HashMap<>();
		// 状态
		Map<String, Object> pStatus = new HashMap<>();
		pStatus.put("value", pOrder.getStatus().ordinal());
		pStatus.put("text", SpringUtils.getMessage("Order.Status." + pOrder.getStatus().name()));
		order.put("status", pStatus);
		order.put("sn", pOrder.getSn());
		order.put("amountPayable", pOrder.getAmountPayable());
		order.put("freight", pOrder.getFreight());
		order.put("createdDate", pOrder.getCreatedDate());
		order.put("consignee", pOrder.getConsignee());
		order.put("address", pOrder.getAddress());
		order.put("phone", pOrder.getPhone());
		order.put("areaName", pOrder.getAreaName());
		order.put("quantity", pOrder.getQuantity());
		
		// 物流信息
		String deliveryCorp = "";
		String trackingNo = "";
		Set<OrderShipping> orderShippings = pOrder.getOrderShippings();
		for (OrderShipping orderShipping : orderShippings) {
			deliveryCorp += orderShipping.getDeliveryCorp();
			trackingNo += orderShipping.getTrackingNo();
		}
		order.put("deliveryCorp", deliveryCorp);
		order.put("trackingNo", trackingNo);
		data.put("order", order);
		
		BigDecimal skuTotal = BigDecimal.ZERO;
		Setting setting = SystemUtils.getSetting();
		List<Map<String, Object>> items = new ArrayList<>();
		for (OrderItem orderItem : pOrder.getOrderItems()) {
			Map<String, Object> item = new HashMap<>();
			Product product = orderItem.getSku().getProduct();
			item.put("itemName", orderItem.getName());
			item.put("thumbnail", StringUtils.isEmpty(orderItem.getThumbnail()) ? setting.getDefaultThumbnailProductImage() : setting.getSiteUrl() + orderItem.getThumbnail());
			item.put("specifications", orderItem.getSpecifications());
			item.put("price", orderItem.getPrice());
			item.put("quantity", orderItem.getQuantity());
			item.put("productId", product.getId());
			items.add(item);
			skuTotal = skuTotal.add(orderItem.getSubtotal());
		}
		order.put("skuTotal", skuTotal);
		order.put("items", items);
		return ResultUtils.ok(data);
	}
	
	
	/**
	 * 取消
	 */
	@PostMapping("/cancel")
	public ApiResult cancel(@CurrentUser Member currentUser, @RequestParam("orderSn") String orderSn) {
		Order order = orderService.findBySn(orderSn);
		if (order == null) {
			return ResultUtils.badRequest("订单不存在");
		}

		if (order.hasExpired() || (!Order.Status.PENDING_PAYMENT.equals(order.getStatus()) && !Order.Status.PENDING_REVIEW.equals(order.getStatus()))) {
			return ResultUtils.unprocessableEntity("订单过期或状态非[等待审核]或[等待付款]");
		}
		if (!orderService.acquireLock(order, currentUser)) {
			return ResultUtils.unprocessableEntity("member.order.locked");
		}
		orderService.cancel(order);
		return ResultUtils.ok();
	}
	
	/**
	 * 收货
	 */
	@PostMapping("/receive")
	public ApiResult receive(@CurrentUser Member currentUser, @RequestParam("orderSn") String orderSn) {
		Order order = orderService.findBySn(orderSn);
		if (order == null) {
			return ResultUtils.badRequest("订单不存在");
		}

		if (order.hasExpired() || !Order.Status.SHIPPED.equals(order.getStatus())) {
			return ResultUtils.badRequest("订单不是[已发货]");
		}
		if (!orderService.acquireLock(order, currentUser)) {
			return ResultUtils.unprocessableEntity("member.order.locked");
		}
		orderService.receive(order);
		return ResultUtils.ok();
	}
	
	/**
	 * 获取支付信息
	 * @param orderSn
	 * @param currentUser
	 * @return
	 */
	private ApiResult getPay(String orderSn, Member currentUser) {
		// 产生支付记录
		PaymentItem paymentItem = new PaymentItem();
		paymentItem.setOrderSn(orderSn);
		paymentItem.setType(PaymentItem.Type.ORDER_PAYMENT);

		PaymentTransaction.LineItem lineItem = paymentTransactionService.generate(paymentItem);
		PaymentPlugin paymentPlugin = pluginService.getPaymentPlugin(WxPayConfiguration.paymentPluginId);
		PaymentTransaction paymentTransaction = paymentTransactionService.generate(lineItem, paymentPlugin, "");
		
		try {
			WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
			HttpServletRequest request = WebUtils.getRequest();

			orderRequest.setBody(StringUtils.abbreviate(getPaymentDescription(paymentTransaction).replaceAll("[^0-9a-zA-Z\\u4e00-\\u9fa5 ]", StringUtils.EMPTY), 600));
			orderRequest.setOutTradeNo(paymentTransaction.getSn());
			orderRequest.setTotalFee(BaseWxPayRequest.yuanToFen(String.valueOf(paymentTransaction.getAmount()))); //元转成分
			orderRequest.setOpenid(currentUser.getUsername());
			orderRequest.setSpbillCreateIp(request.getLocalAddr());
			orderRequest.setTimeStart(DateFormatUtils.format(new Date(), CommonAttributes.DATE_PATTERNS[8]));
			orderRequest.setTimeExpire(DateFormatUtils.format(DateUtils.addMinutes(new Date(),10), CommonAttributes.DATE_PATTERNS[8]));
			orderRequest.setTradeType(WxPayConstants.TradeType.JSAPI);
			WxPayMpOrderResult result = wxService.createOrder(orderRequest);

			_logger.info("\n接收统一下单的消息：appId = [{}], nonceStr = [{}], packageValue = [{}], paySign = [{}], signType = [{}], timeStamp = [{}]",
					result.getAppId(), result.getNonceStr(),result.getPackageValue(), result.getPaySign(), result.getSignType(), result.getTimeStamp());

			return ResultUtils.ok(result);
		} catch (Exception e) {
			_logger.error("微信支付失败！订单号：{},原因:{}", orderSn, e.getMessage());
			e.printStackTrace();
			return ResultUtils.unprocessableEntity("支付失败，请稍后重试！");
		}
	}
	
	/**
	 * 获取支付描述
	 *
	 * @param paymentTransaction
	 *            支付事务
	 * @return 支付描述
	 */
	private String getPaymentDescription(PaymentTransaction paymentTransaction) {
		Assert.notNull(paymentTransaction, "[Assertion failed] - paymentTransaction is required; it must not be null");
		if (CollectionUtils.isEmpty(paymentTransaction.getChildren())) {
			Assert.notNull(paymentTransaction.getType(), "[Assertion failed] - paymentTransaction type is required; it must not be null");
		} else {
			return SpringUtils.getMessage("shop.payment.paymentDescription", paymentTransaction.getSn());
		}

		switch (paymentTransaction.getType()) {
			case ORDER_PAYMENT:
				return SpringUtils.getMessage("shop.payment.orderPaymentDescription", paymentTransaction.getOrder().getSn());
			default:
				return SpringUtils.getMessage("shop.payment.paymentDescription", paymentTransaction.getSn());
		}
	}
	
}
