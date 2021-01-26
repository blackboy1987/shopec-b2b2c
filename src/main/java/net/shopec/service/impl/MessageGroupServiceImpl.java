package net.shopec.service.impl;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.baomidou.mybatisplus.core.metadata.IPage;

import net.shopec.Page;
import net.shopec.Pageable;
import net.shopec.dao.MessageDao;
import net.shopec.dao.MessageGroupDao;
import net.shopec.entity.Message;
import net.shopec.entity.MessageGroup;
import net.shopec.entity.MessageStatus;
import net.shopec.entity.User;
import net.shopec.service.MessageGroupService;

/**
 * Service - 消息组
 * 
 */
@Service
public class MessageGroupServiceImpl extends BaseServiceImpl<MessageGroup> implements MessageGroupService {

	@Inject
	private MessageGroupDao messageGroupDao;
	@Inject
	private MessageDao messageDao;

	@Override
	@Transactional(readOnly = true)
	public Page<MessageGroup> findPage(User user, Pageable pageable) {
		IPage<MessageGroup> iPage = getPluginsPage(pageable);
		iPage.setRecords(messageGroupDao.findPage(iPage, getPageable(pageable), user));
		return super.findPage(iPage, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public MessageGroup find(User user1, User user2) {
		return messageGroupDao.findByUser(user1, user2);
	}

	@Override
	public void delete(MessageGroup messageGroup, User user) {
		Assert.notNull(messageGroup, "[Assertion failed] - messageGroup is required; it must not be null");
		Assert.notNull(user, "[Assertion failed] - user is required; it must not be null");

		MessageStatus user1MessageStatus = messageGroup.getUser1MessageStatus();
		MessageStatus user2MessageStatus = messageGroup.getUser2MessageStatus();
		if (user.equals(messageGroup.getUser1())) {
			user1MessageStatus.setIsDeleted(true);
		} else {
			user2MessageStatus.setIsDeleted(true);
		}
		if (BooleanUtils.isTrue(user1MessageStatus.getIsDeleted()) && BooleanUtils.isTrue(user2MessageStatus.getIsDeleted())) {
			messageGroupDao.remove(messageGroup);
		}

		List<Message> messages = messageDao.findList(messageGroup, user);
		for (Message message : messages) {
			MessageStatus fromUserMessageStatus = message.getFromUserMessageStatus();
			MessageStatus toUserMessageStatus = message.getToUserMessageStatus();
			if (user.equals(message.getFromUser())) {
				fromUserMessageStatus.setIsDeleted(true);
			} else if (user.equals(message.getToUser())) {
				toUserMessageStatus.setIsDeleted(true);
			}
			if (BooleanUtils.isTrue(fromUserMessageStatus.getIsDeleted()) && BooleanUtils.isTrue(toUserMessageStatus.getIsDeleted())) {
				messageDao.remove(message);
			}
		}
	}

}