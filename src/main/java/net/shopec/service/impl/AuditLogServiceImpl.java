package net.shopec.service.impl;

import javax.inject.Inject;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import net.shopec.dao.AuditLogDao;
import net.shopec.entity.AuditLog;
import net.shopec.service.AuditLogService;

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