package com.igomall.controller.member;

import javax.inject.Inject;

import com.igomall.Pageable;
import com.igomall.Results;
import com.igomall.entity.BaseEntity;
import com.igomall.entity.Consultation;
import com.igomall.entity.Member;
import com.igomall.service.ConsultationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.annotation.JsonView;

import com.igomall.exception.UnauthorizedException;
import com.igomall.security.CurrentUser;

/**
 * Controller - 咨询
 * 
 */
@Controller("memberConsultationController")
@RequestMapping("/member/consultation")
public class ConsultationController extends BaseController {

	/**
	 * 每页记录数
	 */
	private static final int PAGE_SIZE = 10;

	@Inject
	private ConsultationService consultationService;

	/**
	 * 添加属性
	 */
	@ModelAttribute
	public void populateModel(Long consultationId, @CurrentUser Member currentUser, ModelMap model) {
		Consultation consultation = consultationService.find(consultationId);
		if (consultation != null && !currentUser.equals(consultation.getMember())) {
			throw new UnauthorizedException();
		}
		model.addAttribute("consultation", consultation);
	}

	/**
	 * 列表
	 */
	@GetMapping("/list")
	public String list(Integer pageNumber, @CurrentUser Member currentUser, ModelMap model) {
		Pageable pageable = new Pageable(pageNumber, PAGE_SIZE);
		model.addAttribute("page", consultationService.findPage(currentUser, null, null, null, pageable));
		return "member/consultation/list";
	}

	/**
	 * 列表
	 */
	@GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(BaseEntity.BaseView.class)
	public ResponseEntity<?> list(Integer pageNumber, @CurrentUser Member currentUser) {
		Pageable pageable = new Pageable(pageNumber, PAGE_SIZE);
		return ResponseEntity.ok(consultationService.findPage(currentUser, null, null, null, pageable).getContent());
	}

	/**
	 * 删除
	 */
	@PostMapping("/delete")
	public ResponseEntity<?> delete(@ModelAttribute(binding = false) Consultation consultation) {
		if (consultation == null) {
			return Results.NOT_FOUND;
		}

		consultationService.delete(consultation);
		return Results.OK;
	}

}