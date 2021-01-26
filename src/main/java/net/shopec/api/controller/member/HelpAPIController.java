package net.shopec.api.controller.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.shopec.api.model.ApiResult;
import net.shopec.api.util.ResultUtils;

/**
 * Controller - 帮助
 * 
 */
@RestController("helpApiCouponCodeController")
@RequestMapping("/api/help")
public class HelpAPIController {

	@Value("${system.name}")
	private String systemName;
	
	@Value("${system.description}")
	private String systemDescription;
	
	/**
	 * 首页
	 */
	@GetMapping
	public ApiResult index() {
		List<Map<String, Object>> helpItems = new ArrayList<>();
		
		Map<String, Object> item1 = new HashMap<>();
		item1.put("title", systemName);
		item1.put("content", systemDescription);
		helpItems.add(item1);
		
		Map<String, Object> item2 = new HashMap<>();
		item2.put("title", "客服电话");
		item2.put("content", "4004228888");
		helpItems.add(item2);
		
		Map<String, Object> data = new HashMap<>();
		data.put("list", helpItems);
		
		return ResultUtils.ok(data);
	}
}
