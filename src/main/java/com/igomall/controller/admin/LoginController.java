package com.igomall.controller.admin;

import com.igomall.entity.Admin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.igomall.security.CurrentUser;

/**
 * Controller - 管理员登录
 * 
 */
@Controller("adminLoginController")
@RequestMapping("/admin")
public class LoginController extends BaseController {

	@Value("${security.admin_login_success_url}")
	private String adminLoginSuccessUrl;

	/**
	 * 登录跳转
	 */
	@GetMapping({ "", "/" })
	public String index() {
		return "redirect:/admin/login";
	}

	/**
	 * 登录页面
	 */
	@GetMapping("/login")
	public String login(@CurrentUser Admin currentUser, ModelMap model) {
		if (currentUser != null) {
			return "redirect:" + adminLoginSuccessUrl;
		}
		model.addAttribute("adminLoginSuccessUrl", adminLoginSuccessUrl);
		return "admin/login/index";
	}

}