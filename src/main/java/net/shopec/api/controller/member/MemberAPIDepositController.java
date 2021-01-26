package net.shopec.api.controller.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.shopec.Page;
import net.shopec.Pageable;
import net.shopec.api.model.ApiResult;
import net.shopec.api.util.ResultUtils;
import net.shopec.entity.Member;
import net.shopec.entity.MemberDepositLog;
import net.shopec.plugin.PaymentPlugin;
import net.shopec.service.MemberDepositLogService;
import net.shopec.service.PluginService;
import net.shopec.service.UserService;
import net.shopec.util.WebUtils;

/**
 * 预存款 - 接口类
 */
@RestController("memberApiDepositController")
@RequestMapping("/api/member/member_deposit")
public class MemberAPIDepositController extends BaseAPIController {

	/**
	 * 每页记录数
	 */
	private static final int PAGE_SIZE = 10;

	@Inject
	private MemberDepositLogService memberDepositLogService;
	@Inject
	private PluginService pluginService;
	@Inject
	private UserService userService;

	/**
	 * 检查余额
	 */
	@PostMapping("/check_balance")
	public ApiResult checkBalance() {
		Member currentUser = userService.getCurrent(Member.class);
		Map<String, Object> data = new HashMap<>();
		data.put("balance", currentUser.getBalance());
		return ResultUtils.ok(data);
	}

	/**
	 * 充值
	 */
	@GetMapping("/recharge")
	public ApiResult recharge() {
		List<PaymentPlugin> paymentPlugins = pluginService.getActivePaymentPlugins(WebUtils.getRequest());
		Map<String, Object> data = new HashMap<>();
		if (!paymentPlugins.isEmpty()) {
			Map<String, Object> defaultPlugin = new HashMap<>();
			defaultPlugin.put("id", paymentPlugins.get(0).getId());
			defaultPlugin.put("displayName", paymentPlugins.get(0).getDisplayName());
			defaultPlugin.put("logo", paymentPlugins.get(0).getLogo());
			data.put("defaultPaymentPlugin", defaultPlugin);
			
			List<Map<String, Object>> paymentPluginsMap = new ArrayList<>();
			for (PaymentPlugin paymentPlugin : paymentPlugins) {
				Map<String, Object> item = new HashMap<>();
				item.put("id", paymentPlugin.getId());
				item.put("displayName", paymentPlugin.getDisplayName());
				item.put("logo", paymentPlugin.getLogo());
				paymentPluginsMap.add(item);
			}
			data.put("paymentPlugins", paymentPluginsMap);
		}
		return ResultUtils.ok(data);
	}


	/**
	 * 记录
	 */
	@GetMapping("/log")
	public ApiResult log(@RequestParam(name = "pageNumber", defaultValue = "1")Integer pageNumber) {
		Member currentUser = userService.getCurrent(Member.class);
		
		List<Map<String, Object>> data = new ArrayList<>();
		Pageable pageable = new Pageable(pageNumber, PAGE_SIZE);
		Page<MemberDepositLog> memberDepositLogs = memberDepositLogService.findPage(currentUser, pageable);
		for (MemberDepositLog memberDepositLog : memberDepositLogs.getContent()) {
			Map<String, Object> item = new HashMap<>();
			item.put("type", memberDepositLog.getType());
			item.put("credit", memberDepositLog.getCredit());
			item.put("debit", memberDepositLog.getDebit());
			item.put("balance", memberDepositLog.getBalance());
			item.put("memo", memberDepositLog.getMemo());
			data.add(item);
		}
		return ResultUtils.ok(data);
	}

}