package net.shopec.controller.business;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import net.shopec.entity.Business;
import net.shopec.security.CurrentUser;

/**
 * Controller - 商家登录
 * 
 */
@Controller("businessLoginController")
@RequestMapping("/business")
public class LoginController extends BaseController {

	@Value("${security.business_login_success_url}")
	private String businessLoginSuccessUrl;

	/**
	 * 登录跳转
	 */
	@GetMapping({ "", "/" })
	public String index() {
		return "redirect:/business/login";
	}

	/**
	 * 登录页面
	 */
	@GetMapping("/login")
	public String index(@CurrentUser Business currentUser, ModelMap model) {
		if (currentUser != null) {
			return "redirect:" + businessLoginSuccessUrl;
		}
		model.addAttribute("businessLoginSuccessUrl", businessLoginSuccessUrl);
		return "business/login/index";
	}

}