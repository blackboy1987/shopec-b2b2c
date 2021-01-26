package net.shopec.api.controller.shop;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wf.jwtp.exception.ExpiredTokenException;
import org.wf.jwtp.provider.Token;
import org.wf.jwtp.provider.TokenStore;

import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.binarywang.wx.miniapp.config.WxMaInMemoryConfig;
import me.chanjar.weixin.common.error.WxErrorException;
import net.shopec.CommonAttributes;
import net.shopec.api.model.ApiResult;
import net.shopec.api.util.ResultUtils;
import net.shopec.entity.Member;
import net.shopec.entity.Receiver;
import net.shopec.entity.SocialUser;
import net.shopec.plugin.LoginPlugin;
import net.shopec.service.MemberRankService;
import net.shopec.service.PluginService;
import net.shopec.service.ReceiverService;
import net.shopec.service.SocialUserService;
import net.shopec.service.UserService;

/**
 * Controller - 社会化用户登录
 * 
 */
@RestController
@RequestMapping("/api/social_user")
public class SocialUserLoginAPIController {

	/**
	 * 小程序登录id
	 */
	private static final String loginPluginId = "weixinMiniLoginPlugin";
	
	private static final Logger _logger = LoggerFactory.getLogger(SocialUserLoginAPIController.class);
	
	@Inject
	private UserService userService;
	@Inject
	private SocialUserService socialUserService;
	@Inject
	private PluginService pluginService;
	@Inject
	private MemberRankService memberRankService;
	@Inject
	private ReceiverService receiverService;
	@Inject
	private TokenStore tokenStore;
	
	private final WxMaServiceImpl wxMaService = new WxMaServiceImpl();
	
	private LoginPlugin loginPlugin;
	
	
	@ModelAttribute
	public void populateModel() {
		loginPlugin = pluginService.getLoginPlugin(loginPluginId);
		// 获取小程序服务实例
		WxMaInMemoryConfig wxMaInMemoryConfig = new WxMaInMemoryConfig();
//		wxMaInMemoryConfig.setAppid(loginPlugin.getAttribute("appId"));
//		wxMaInMemoryConfig.setSecret(loginPlugin.getAttribute("appSecret"));

		wxMaInMemoryConfig.setAppid("wx28c3d173fa4e12ff");
		wxMaInMemoryConfig.setSecret("d42078220d76d99dfed734210bb4716f");

		wxMaService.setWxMaConfig(wxMaInMemoryConfig);
	}
	
	/**
	 * 登录接口
	 */
	@PostMapping("/login")
	public ApiResult signIn(String code, String signature, String rawData, String encryptedData, String ivStr) {
		_logger.info("用户请求登录获取Token");
		
		if (StringUtils.isEmpty(code)) {
			throw new ExpiredTokenException();
		}
		
		if (loginPlugin == null || BooleanUtils.isNotTrue(loginPlugin.getIsEnabled())) {
			return ResultUtils.badRequest("现在禁止登录!");
		}
		
		WxMaJscode2SessionResult codeResult = null;
		try {
			codeResult = wxMaService.jsCode2SessionInfo(code);
		} catch (WxErrorException e) {
			e.printStackTrace();
			_logger.info("error :[{}] " , e.getMessage());
		}
		_logger.info("codeResult : [{}]" , codeResult);
		
		String unionid = null;
		if (StringUtils.isNotBlank(codeResult.getUnionid())) {
			unionid = codeResult.getUnionid();
		} else {
			unionid = codeResult.getOpenid();
			_logger.info("小程序未绑定微信开放平台，改为取openid[{}] : ", unionid);
		}
		
		SocialUser socialUser = socialUserService.find(loginPluginId, unionid);
		Map<String, Object> data = new HashMap<>();
		if (socialUser != null) {
			if (socialUser.getUser() != null) {
				Member pMember = (Member)socialUser.getUser();
				Map<String, Object> member = new HashMap<>();
		        member.put("id", String.valueOf(pMember.getId()));
		        member.put("nickName", pMember.getName());
		        member.put("avatarUrl", pMember.getAvatarUrl());
		        member.put("point", pMember.getPoint());
		        
		        // 签发token
				Token token = tokenStore.createNewToken(String.valueOf(pMember.getId()), Member.MEMBER_PERMISSIONS, Member.ROLES, CommonAttributes.EXPIRE_TIME);
				data.put("token", "Bearer " + token.getAccessToken());
				data.put("member", member);
				return ResultUtils.ok(data);
			}
		} else {
			socialUser = new SocialUser();
			socialUser.setLoginPluginId(loginPluginId);
			socialUser.setUniqueId(unionid);
			socialUser.setUser(null);
			socialUserService.save(socialUser);
		}
		
		WxMaUserService wxMaUserService = wxMaService.getUserService();
		String sessionKey = codeResult.getSessionKey();
		
        // 用户信息校验
        if (!wxMaUserService.checkUserInfo(sessionKey, rawData, signature)) {
            return ResultUtils.badRequest("验证用户信息完整性失败,请重试!");
        }
        
        // 解密用户信息
        WxMaUserInfo wxMaUserInfo = wxMaUserService.getUserInfo(sessionKey, encryptedData, ivStr);
        Member member = new Member();
        if (wxMaUserInfo != null) {
			member.setUsername(unionid);
			member.setPassword(RandomStringUtils.random(8));
			member.setName(wxMaUserInfo.getNickName());
			member.setGender(StringUtils.equals(wxMaUserInfo.getGender(), "1") ? Member.Gender.MALE : Member.Gender.FEMALE);
			member.setAddress(wxMaUserInfo.getCountry() + ", " + wxMaUserInfo.getProvince() + ", " + wxMaUserInfo.getCity());
			member.setAvatarUrl(wxMaUserInfo.getAvatarUrl());
    		member.setEmail(null);
    		member.setPoint(0L);
    		member.setBalance(BigDecimal.ZERO);
    		member.setFrozenAmount(BigDecimal.ZERO);
    		member.setAmount(BigDecimal.ZERO);
    		member.setIsEnabled(true);
    		member.setIsLocked(false);
    		member.setLockDate(null);
    		member.setLastLoginIp(null);
    		member.setLastLoginDate(new Date());
    		member.setSafeKey(null);
    		member.setMemberRank(memberRankService.findDefault());
    		member.setDistributor(null);
    		member.setCart(null);
    		member.setOrders(null);
    		member.setPaymentTransactions(null);
    		member.setMemberDepositLogs(null);
    		member.setCouponCodes(null);
    		member.setReceivers(null);
    		member.setReviews(null);
    		member.setConsultations(null);
    		member.setProductFavorites(null);
    		member.setProductNotifies(null);
    		member.setSocialUsers(null);
    		member.setPointLogs(null);
    		userService.register(member);
    		
    		// 绑定用户
			String uniqueId = wxMaUserInfo.getUnionId();
			String socialUserId = String.valueOf(socialUser.getId());
			if (StringUtils.isNotEmpty(socialUserId) && StringUtils.isNotEmpty(uniqueId)) {
				SocialUser pSocialUser = socialUserService.find(Long.parseLong(socialUserId));
				if (socialUser != null && socialUser.getUser() == null) {
					socialUserService.bindUser(member, pSocialUser, uniqueId);
				}
			}
			
        }
        Map<String, Object> item = new HashMap<>();
        item.put("id", String.valueOf(member.getId()));
        item.put("nickName", member.getName());
        item.put("avatarUrl", member.getAvatarUrl());
        item.put("point", member.getPoint());
        item.put("address", member.getReceivers());
        item.put("open_id", member.getUsername());
        
        // 默认地址
        Receiver defaultReceiver = receiverService.findDefault(member);
        if (defaultReceiver != null) {
        	item.put("address_default", defaultReceiver);
            item.put("address_id", String.valueOf(defaultReceiver.getId()));
        }
        
        // 签发token
		Token token = tokenStore.createNewToken(String.valueOf(member.getId()), Member.MEMBER_PERMISSIONS, Member.ROLES, CommonAttributes.EXPIRE_TIME);
		data.put("token", "Bearer " + token.getAccessToken());
		data.put("member", item);
        return ResultUtils.ok(data);
	}
	
}