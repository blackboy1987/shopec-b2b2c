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
import net.shopec.dao.ReceiverDao;
import net.shopec.entity.Member;
import net.shopec.entity.Receiver;
import net.shopec.service.ReceiverService;

/**
 * Service - 收货地址
 * 
 */
@Service
public class ReceiverServiceImpl extends BaseServiceImpl<Receiver> implements ReceiverService {

	@Inject
	private ReceiverDao receiverDao;

	@Override
	@Transactional(readOnly = true)
	public Receiver findDefault(Member member) {
		return receiverDao.findDefault(member);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Receiver> findList(Member member) {
		return receiverDao.findList(member);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Receiver> findPage(Member member, Pageable pageable) {
		IPage<Receiver> iPage = getPluginsPage(pageable);
		iPage.setRecords(receiverDao.findPage(iPage, getPageable(pageable), member));
		return super.findPage(iPage, pageable);
	}

	@Override
	@Transactional
	public boolean save(Receiver receiver) {
		Assert.notNull(receiver, "[Assertion failed] - receiver is required; it must not be null");

		if (BooleanUtils.isTrue(receiver.getIsDefault()) && receiver.getMember() != null) {
			receiverDao.clearDefault(receiver.getMember());
		}
		if (receiver.getArea() != null) {
			receiver.setAreaName(receiver.getArea().getFullName());
		}
		return super.save(receiver);
	}

	@Override
	@Transactional
	public Receiver update(Receiver receiver) {
		Assert.notNull(receiver, "[Assertion failed] - receiver is required; it must not be null");

		if (receiver.getArea() != null) {
			receiver.setAreaName(receiver.getArea().getFullName());
		}
		Receiver pReceiver = super.update(receiver);
		if (BooleanUtils.isTrue(pReceiver.getIsDefault()) && pReceiver.getMember() != null) {
			receiverDao.clearDefaultExclude(pReceiver.getMember(), pReceiver);
		}
		return pReceiver;
	}

}