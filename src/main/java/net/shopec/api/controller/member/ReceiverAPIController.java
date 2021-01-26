package net.shopec.api.controller.member;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.BeanUtils;
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
import net.shopec.entity.Receiver;
import net.shopec.security.CurrentUser;
import net.shopec.service.AreaService;
import net.shopec.service.ReceiverService;

/**
 * 收货地址 - 接口类
 */
@RestController("memberApiReceiverController")
@RequestMapping("/api/member/receiver")
public class ReceiverAPIController extends BaseAPIController {

	@Inject
	private AreaService areaService;
	@Inject
	private ReceiverService receiverService;

	/**
	 * 列表
	 */
	@GetMapping("/list")
	public ApiResult list(@CurrentUser Member currentUser) {
		Map<String, Object> data = new HashMap<>();
		Receiver defaultReceiver = receiverService.findDefault(currentUser);
		data.put("defaultId", String.valueOf(defaultReceiver.getId()));
		
		List<Map<String, Object>> list = new ArrayList<>();
		Page<Receiver> receivers = receiverService.findPage(currentUser, new Pageable());
		for (Receiver receiver : receivers.getContent()) {
			Map<String, Object> item = new HashMap<>();
			item.put("receiverId", String.valueOf(receiver.getId()));
			item.put("consignee", receiver.getConsignee());
			item.put("areaName", receiver.getAreaName());
			item.put("address", receiver.getAddress());
			item.put("phone", receiver.getPhone());
			list.add(item);
		}
		data.put("list", list);
		return ResultUtils.ok(data);
	}


	/**
	 * 编辑
	 */
	@GetMapping("/edit")
	public ApiResult edit(@RequestParam("receiverId") Long receiverId) {
		if (receiverId == null) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		Receiver pReceiver = receiverService.find(receiverId);
		Map<String, Object> data = new HashMap<>();
		
		Map<String, Object> receiver = new HashMap<>();
		receiver.put("receiverId", String.valueOf(pReceiver.getId()));
		receiver.put("consignee", pReceiver.getConsignee());
		//receiver.put("areaName", Arrays.asList(pReceiver.getAreaName()));
		receiver.put("address", pReceiver.getAddress());
		receiver.put("phone", pReceiver.getPhone());
		
		data.put("areaName", Arrays.asList(pReceiver.getAreaName()));
		data.put("receiver", receiver);
		return ResultUtils.ok(data);
	}
	
	/**
	 * 保存
	 */
	@PostMapping("/save")
	public ApiResult save(@CurrentUser Member currentUser, Receiver receiver) {
		receiver.setArea(areaService.findByFullName(receiver.getAreaName()));
		receiver.setIsDefault(true);
		if (!isValid(receiver)) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		if (Receiver.MAX_RECEIVER_COUNT != null && currentUser.getReceivers().size() >= Receiver.MAX_RECEIVER_COUNT) {
			return ResultUtils.unprocessableEntity("member.receiver.addCountNotAllowed", Receiver.MAX_RECEIVER_COUNT);
		}
		receiver.setAreaName(null);
		receiver.setMember(currentUser);
		receiverService.save(receiver);
		return ResultUtils.ok();
	}


	/**
	 * 更新
	 */
	@PostMapping("/update")
	public ApiResult update(@CurrentUser Member currentUser, Receiver receiverForm) {
		Receiver receiver = receiverService.find(receiverForm.getId());

		receiverForm.setArea(areaService.findByFullName(receiver.getAreaName()));
		receiverForm.setMember(currentUser);
		receiverForm.setIsDefault(receiver.getIsDefault());
		if (!isValid(receiverForm)) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		BeanUtils.copyProperties(receiverForm, receiver, "id", "member");
		receiverService.updateById(receiver);
		return ResultUtils.ok();
	}

	/**
	 * 设置默认
	 */
	@PostMapping("/update_default")
	public ApiResult updateDefault(@RequestParam("receiverId") Long receiverId) {
		Receiver receiver = receiverService.find(receiverId);
		if (receiver == null) {
			return ResultUtils.NOT_FOUND;
		}
		receiver.setIsDefault(true);
		receiverService.update(receiver);
		return ResultUtils.ok();
	}
	
	/**
	 * 删除
	 */
	@PostMapping("/delete")
	public ApiResult delete(@RequestParam("receiverId") Long receiverId) {
		Receiver receiver = receiverService.find(receiverId);
		if (receiver == null) {
			return ResultUtils.NOT_FOUND;
		}

		receiverService.delete(receiver);
		return ResultUtils.ok();
	}

}