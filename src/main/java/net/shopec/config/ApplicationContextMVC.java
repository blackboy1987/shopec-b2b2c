package net.shopec.config;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mobile.device.DeviceWebArgumentResolver;
import org.springframework.mobile.device.site.SitePreferenceWebArgumentResolver;
import org.springframework.mobile.device.view.LiteDeviceDelegatingViewResolver;
import org.springframework.stereotype.Controller;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.WebContentInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.ServletWebArgumentResolverAdapter;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import net.shopec.Setting;
import net.shopec.audit.AuditLogInterceptor;
import net.shopec.audit.AuditLogMethodArgumentResolver;
import net.shopec.captcha.CaptchaInterceptor;
import net.shopec.entity.Admin;
import net.shopec.entity.Business;
import net.shopec.entity.Member;
import net.shopec.interceptor.PromotionPluginInterceptor;
import net.shopec.security.CsrfInterceptor;
import net.shopec.security.CurrentCartHandlerInterceptor;
import net.shopec.security.CurrentCartMethodArgumentResolver;
import net.shopec.security.CurrentStoreHandlerInterceptor;
import net.shopec.security.CurrentStoreMethodArgumentResolver;
import net.shopec.security.CurrentUserHandlerInterceptor;
import net.shopec.security.CurrentUserMethodArgumentResolver;
import net.shopec.security.XssInterceptor;
import net.shopec.security.XssInterceptor.WhitelistType;

@Configuration
@PropertySource("classpath:shopec.properties")
@ComponentScan(includeFilters = {
		@org.springframework.context.annotation.ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Controller.class),
		@org.springframework.context.annotation.ComponentScan.Filter(type = FilterType.ANNOTATION, classes = ControllerAdvice.class) })
public class ApplicationContextMVC implements WebMvcConfigurer {

	@Value("${html_content_type}")
	private String contentType;
	
	@Value("${template.suffix}")
	private String suffix;
	
	@Value("${json_content_type}")
	private String jsonContentType;
	
	@Value("${html_content_type}")
	private String htmlContentType;
	
	
	@Inject
	private MessageSource messageSource;
	
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new ServletWebArgumentResolverAdapter(new DeviceWebArgumentResolver()));
		resolvers.add(new ServletWebArgumentResolverAdapter(new SitePreferenceWebArgumentResolver()));
		resolvers.add(currentUserMethodArgumentResolver());
		resolvers.add(currentCartMethodArgumentResolver());
		resolvers.add(currentStoreMethodArgumentResolver());
		resolvers.add(auditLogMethodArgumentResolver());
	}
	
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		ObjectMapper objectMapper = new ObjectMapper();
        // null字段不返回
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // mybatis 使用懒加载后，返回JSON报错
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		
		converters.add(new BufferedImageHttpMessageConverter());
		MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
		supportedMediaTypes.add(MediaType.parseMediaType(jsonContentType));
		supportedMediaTypes.add(MediaType.parseMediaType(htmlContentType));
		
		jackson2HttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);
		jackson2HttpMessageConverter.setObjectMapper(objectMapper);
		converters.add(jackson2HttpMessageConverter);
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:static/favicon.ico").setCachePeriod(86400);
		registry.addResourceHandler("/robots.txt").addResourceLocations("classpath:static/robots.txt").setCachePeriod(86400);
		registry.addResourceHandler("/resources/**").addResourceLocations("classpath:static/resources/").setCachePeriod(86400);
		// 本地测试时要这样配置实际的目录
		registry.addResourceHandler("/upload/**").addResourceLocations("classpath:static/upload/").setCachePeriod(86400);
		registry.addResourceHandler("/6.0/**").addResourceLocations("classpath:static/6.0/").setCachePeriod(86400);
		
//		registry.addResourceHandler("/upload/**").addResourceLocations("file:/opt/demo/shopec-img/upload/").setCachePeriod(86400);
//		registry.addResourceHandler("/6.0/**").addResourceLocations("file:/opt/demo/shopec-img/shopec-b2b2c/6.0/").setCachePeriod(86400);
	}
	
	@Bean
	public WebContentInterceptor resourcesInterceptor() {
		WebContentInterceptor resourcesInterceptor = new WebContentInterceptor();
		resourcesInterceptor.setCacheSeconds(86400);
		return resourcesInterceptor;
	}
	
	@Bean
	public WebContentInterceptor webContentInterceptor() {
		WebContentInterceptor webContentInterceptor = new WebContentInterceptor();
		webContentInterceptor.setCacheSeconds(0);
		return webContentInterceptor;
	}
	
	/**
	 * 当前用户MethodArgumentResolver
	 */
	@Bean
	public CurrentUserMethodArgumentResolver currentUserMethodArgumentResolver() {
		return new CurrentUserMethodArgumentResolver();
	}
	
	/**
	 * 当前购物车MethodArgumentResolver
	 */
	@Bean
	public CurrentCartMethodArgumentResolver currentCartMethodArgumentResolver() {
		return new CurrentCartMethodArgumentResolver();
	}
	
	/**
	 * 当前店铺MethodArgumentResolver
	 */
	@Bean
	public CurrentStoreMethodArgumentResolver currentStoreMethodArgumentResolver() {
		return new CurrentStoreMethodArgumentResolver();
	}
	
	/**
	 * 审计日志MethodArgumentResolver
	 */
	@Bean
	public AuditLogMethodArgumentResolver auditLogMethodArgumentResolver() {
		return new AuditLogMethodArgumentResolver();
	}
	
	/**
	 * 当前用户拦截器
	 */
	@Bean
	public CurrentUserHandlerInterceptor memberInterceptor() {
		CurrentUserHandlerInterceptor memberInterceptor = new CurrentUserHandlerInterceptor();
		memberInterceptor.setUserClass(Member.class);
		return memberInterceptor;
	}
	
	/**
	 * 当前商家拦截器
	 */
	@Bean
	public CurrentUserHandlerInterceptor businessInterceptor() {
		CurrentUserHandlerInterceptor businessInterceptor = new CurrentUserHandlerInterceptor();
		businessInterceptor.setUserClass(Business.class);
		return businessInterceptor;
	}
	
	/**
	 * 当前管理员拦截器
	 */
	@Bean
	public CurrentUserHandlerInterceptor adminInterceptor() {
		CurrentUserHandlerInterceptor adminInterceptor = new CurrentUserHandlerInterceptor();
		adminInterceptor.setUserClass(Admin.class);
		return adminInterceptor;
	}
	
	/**
	 * 当前购物车拦截器
	 */
	@Bean
	public CurrentCartHandlerInterceptor currentCartHandlerInterceptor() {
		return new CurrentCartHandlerInterceptor();
	}
	
	/**
	 * 当前店铺拦截器
	 */
	@Bean
	public CurrentStoreHandlerInterceptor currentStoreHandlerInterceptor() {
		return new CurrentStoreHandlerInterceptor();
	}
	
	/**
	 * CSRF拦截器
	 */
	@Bean
	public CsrfInterceptor csrfInterceptor() {
		return new CsrfInterceptor();
	}
	
	/**
	 * XSS拦截器
	 */
	@Bean
	public XssInterceptor xssInterceptor() {
		return new XssInterceptor();
	}
	
	/**
	 * XSS拦截器XSS拦截器白名单XSS拦截器白名单
	 */
	@Bean
	public XssInterceptor whiteListXssInterceptor() {
		XssInterceptor xssInterceptor = new XssInterceptor();
		xssInterceptor.setWhitelistType(WhitelistType.RELAXED);
		return xssInterceptor;
	}
	
	/**
	 * 会员验证码拦截器
	 */
	@Bean
	public CaptchaInterceptor memberRegister() {
		CaptchaInterceptor memberRegister = new CaptchaInterceptor();
		memberRegister.setCaptchaType(Setting.CaptchaType.MEMBER_REGISTER);
		return memberRegister;
	}
	
	/**
	 * 商家验证码拦截器
	 */
	@Bean
	public CaptchaInterceptor businessRegister() {
		CaptchaInterceptor businessRegister = new CaptchaInterceptor();
		businessRegister.setCaptchaType(Setting.CaptchaType.BUSINESS_REGISTER);
		return businessRegister;
	}
	
	/**
	 * 评论验证码拦截器
	 */
	@Bean
	public CaptchaInterceptor review() {
		CaptchaInterceptor review = new CaptchaInterceptor();
		review.setCaptchaType(Setting.CaptchaType.REVIEW);
		return review;
	}
	
	/**
	 * 咨询验证码拦截器
	 */
	@Bean
	public CaptchaInterceptor consultation() {
		CaptchaInterceptor consultation = new CaptchaInterceptor();
		consultation.setCaptchaType(Setting.CaptchaType.CONSULTATION);
		return consultation;
	}
	
	/**
	 * 找回密码验证码拦截器
	 */
	@Bean
	public CaptchaInterceptor forgotPassword() {
		CaptchaInterceptor resetPassword = new CaptchaInterceptor();
		resetPassword.setCaptchaType(Setting.CaptchaType.FORGOT_PASSWORD);
		return resetPassword;
	}
	
	/**
	 * 找回密码验证码拦截器
	 */
	@Bean
	public CaptchaInterceptor resetPassword() {
		CaptchaInterceptor resetPassword = new CaptchaInterceptor();
		resetPassword.setCaptchaType(Setting.CaptchaType.RESET_PASSWORD);
		return resetPassword;
	}
	
	/**
	 * 审计日志拦截器
	 */
	@Bean
	public AuditLogInterceptor auditLogInterceptor() {
		return new AuditLogInterceptor();
	}
	
	/**
	 * 促销插件增加
	 */
	@Bean
	public PromotionPluginInterceptor promotionPluginInterceptorAdd() {
		return new PromotionPluginInterceptor();
	}
	
	/**
	 * 促销插件编辑
	 */
	@Bean
	public PromotionPluginInterceptor promotionPluginInterceptorEdit() {
		PromotionPluginInterceptor promotionPluginInterceptor = new PromotionPluginInterceptor();
		PromotionPluginInterceptor.PromotionIdPromotionPluginProvider promotionIdPromotionPluginProvider = new PromotionPluginInterceptor.PromotionIdPromotionPluginProvider();
		promotionPluginInterceptor.setPromotionPluginProvider(promotionIdPromotionPluginProvider);
		return promotionPluginInterceptor;
	}
	
	/**
	 * 促销插件删除
	 */
	@Bean
	public PromotionPluginInterceptor promotionPluginInterceptorDelete() {
		PromotionPluginInterceptor promotionPluginInterceptor = new PromotionPluginInterceptor();
		PromotionPluginInterceptor.PromotionIdPromotionPluginProvider promotionIdPromotionPluginProvider = new PromotionPluginInterceptor.PromotionIdPromotionPluginProvider();
		promotionIdPromotionPluginProvider.setPromotionIdParameterName("ids");
		promotionPluginInterceptor.setPromotionPluginProvider(promotionIdPromotionPluginProvider);
		return promotionPluginInterceptor;
	}
	
	
	/**
	 * 增加拦截器
	 */
	public void addInterceptors(InterceptorRegistry registry) {
		// RESOURCES
		registry.addInterceptor(resourcesInterceptor()).addPathPatterns("/resources/**");
		// WEB
		registry.addInterceptor(webContentInterceptor()).addPathPatterns("/cart/**", "/order/**", "/member/**", "/business/**", "/admin/**");
		// MEMBER
		registry.addInterceptor(memberInterceptor()).addPathPatterns("/cart/**", "/order/**", "/member/**");
		// BUSINESS
		registry.addInterceptor(businessInterceptor()).addPathPatterns("/business/**");
		// ADMIN
		registry.addInterceptor(adminInterceptor()).addPathPatterns("/admin/**");
		// CART
		registry.addInterceptor(currentCartHandlerInterceptor()).addPathPatterns("/cart/**", "/order/**");
		// STORE
		registry.addInterceptor(currentStoreHandlerInterceptor()).addPathPatterns("/business/**");
		// CSRF
		registry.addInterceptor(csrfInterceptor()).addPathPatterns("/**").excludePathPatterns("/payment/**", "/api/**");
		// XSS拦截器
		registry.addInterceptor(xssInterceptor()).addPathPatterns("/**").excludePathPatterns("/admin/**", "/business/**");
		// XSS拦截器白名单
		registry.addInterceptor(whiteListXssInterceptor()).addPathPatterns("/admin/**", "/business/**").excludePathPatterns("/admin/template/**", "/admin/ad_position/**");
		
		// memberCaptcha
		registry.addInterceptor(memberRegister()).addPathPatterns("/member/register/submit");
		// businessCaptcha
		registry.addInterceptor(businessRegister()).addPathPatterns("/business/register/submit");
		// consultationCaptcha
		registry.addInterceptor(consultation()).addPathPatterns("/consultation/save");
		// forgotPasswordCaptcha
		registry.addInterceptor(forgotPassword()).addPathPatterns("/password/forgot");
		// resetPasswordCaptcha
		registry.addInterceptor(resetPassword()).addPathPatterns("/password/reset");
		// auditLog
		registry.addInterceptor(auditLogInterceptor()).addPathPatterns("/admin/**");
		// 促销插件增加
		registry.addInterceptor(promotionPluginInterceptorAdd()).addPathPatterns("/business/**promotion/add", "/business/**promotion/save", "/business/promotion_plugin/list");
		// 促销插件编辑
		registry.addInterceptor(promotionPluginInterceptorEdit()).addPathPatterns("/business/**promotion/edit", "/business/**promotion/update");
		// 促销插件删除
		registry.addInterceptor(promotionPluginInterceptorDelete()).addPathPatterns("/business/promotion/delete");
		
	}
	
	@Bean
	public LocalValidatorFactoryBean validator() {
		LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
		localValidatorFactoryBean.setValidationMessageSource(messageSource);
		return localValidatorFactoryBean;
	}
	
	@Bean
	public LiteDeviceDelegatingViewResolver viewResolver(){
		FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
		resolver.setContentType(contentType);
		resolver.setSuffix(suffix);
		
		LiteDeviceDelegatingViewResolver viewResolver = new LiteDeviceDelegatingViewResolver(resolver);
		viewResolver.setMobilePrefix("mobile/");
		viewResolver.setTabletPrefix("tablet/");
		viewResolver.setEnableFallback(true);
	    return viewResolver;
	}
	
	
}
