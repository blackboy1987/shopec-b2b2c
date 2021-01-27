package com.igomall.service;

import com.igomall.entity.MessageConfig;

/**
 * Service - 消息配置
 * 
 */
public interface MessageConfigService extends BaseService<MessageConfig> {

	/**
	 * 查找消息配置
	 * 
	 * @param type
	 *            类型
	 * @return 消息配置
	 */
	MessageConfig findByType(MessageConfig.Type type);

}