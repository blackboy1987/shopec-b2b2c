package com.igomall.service.impl;

import javax.inject.Inject;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.igomall.dao.AuditLogDao;
import com.igomall.entity.AuditLog;
import com.igomall.service.AuditLogService;

/**
 * Service - 审计日志
 * 
 */
@Service
public class AuditLogServiceImpl extends BaseServiceImpl<AuditLog> implements AuditLogService {

	@Inject
	private AuditLogDao auditLogDao;

	@Override
	@Async
	public void create(AuditLog auditLog) {
		super.save(auditLog);
	}

	@Override
	public void clear() {
		auditLogDao.removeAll();
	}

}