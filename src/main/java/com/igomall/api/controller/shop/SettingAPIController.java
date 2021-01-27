package com.igomall.api.controller.shop;

import java.util.HashMap;
import java.util.Map;

import com.igomall.util.SystemUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.igomall.Setting;
import com.igomall.api.model.ApiResult;
import com.igomall.api.util.ResultUtils;

/**
 * 小程序设置 - 设置接口类
 */
@RestController
@RequestMapping("/api/setting")
public class SettingAPIController {

	/**
	 * 设置
	 */
	@GetMapping
	public ApiResult index() {
		Setting setting = SystemUtils.getSetting();
		Map<String, Object> topTextColor = new HashMap<>();
		topTextColor.put("text", "#ffffff");
		topTextColor.put("value", "20");
		
		Map<String, Object> wxapp = new HashMap<>();
		wxapp.put("top_background_color", "#00aa00");
		wxapp.put("wxapp_title", setting.getSiteName());
		
		wxapp.put("top_text_color", topTextColor);
		
		Map<String, Object> data = new HashMap<>();
		data.put("wxapp", wxapp);
		
		return ResultUtils.ok(data);
	}

}
