package net.shopec.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import net.shopec.Pageable;
import net.shopec.Results;
import net.shopec.entity.Admin;
import net.shopec.entity.Member;
import net.shopec.entity.PointLog;
import net.shopec.security.CurrentUser;
import net.shopec.service.MemberService;
import net.shopec.service.PointLogService;

/**
 * Controller - 积分
 * 
 */
@Controller("adminPointController")
@RequestMapping("/admin/point")
public class PointController extends BaseController {

	@Inject
	private PointLogService pointLogService;
	@Inject
	private MemberService memberService;

	/**
	 * 会员选择
	 */
	@GetMapping("/member_select")
	public ResponseEntity<?> memberSelect(String keyword) {
		List<Map<String, Object>> data = new ArrayList<>();
		if (StringUtils.isEmpty(keyword)) {
			return ResponseEntity.ok(data);
		}
		List<Member> members = memberService.search(keyword, null, null);
		for (Member member : members) {
			Map<String, Object> item = new HashMap<>();
			item.put("id", String.valueOf(member.getId()));
			item.put("name", member.getUsername());
			item.put("point", member.getPoint());
			data.add(item);
		}
		return ResponseEntity.ok(data);
	}

	/**
	 * 调整
	 */
	@GetMapping("/adjust")
	public String adjust() {
		return "admin/point/adjust";
	}

	/**
	 * 调整
	 */
	@PostMapping("/adjust")
	public ResponseEntity<?> adjust(Long memberId, long amount, String memo, @CurrentUser Admin currentUser) {
		Member member = memberService.find(memberId);
		if (member == null) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		if (amount == 0) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		if (member.getPoint() == null || member.getPoint() + amount < 0) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		memberService.addPoint(member, amount, PointLog.Type.ADJUSTMENT, memo);
		return Results.OK;
	}

	/**
	 * 记录
	 */
	@GetMapping("/log")
	public String log(Long memberId, Pageable pageable, ModelMap model) {
		Member member = memberService.find(memberId);
		if (member != null) {
			model.addAttribute("member", member);
			model.addAttribute("page", pointLogService.findPage(member, pageable));
		} else {
			model.addAttribute("page", pointLogService.findPage(pageable));
		}
		return "admin/point/log";
	}

}