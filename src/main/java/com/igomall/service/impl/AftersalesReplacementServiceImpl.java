package com.igomall.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.igomall.dao.AftersalesItemDao;
import com.igomall.dao.AftersalesReplacementDao;
import com.igomall.entity.AftersalesItem;
import com.igomall.entity.AftersalesReplacement;
import com.igomall.service.AftersalesReplacementService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * Service - 换货
 * 
 */
@Service
public class AftersalesReplacementServiceImpl extends ServiceImpl<AftersalesReplacementDao, AftersalesReplacement> implements AftersalesReplacementService {

	@Inject
	private AftersalesReplacementDao aftersalesReplacementDao;
	@Inject
	private AftersalesItemDao aftersalesItemDao;

	@Override
	public AftersalesReplacement find(Long id) {
		return aftersalesReplacementDao.find(id);
	}
	
	@Override
	@Transactional
	public boolean save(AftersalesReplacement aftersalesReplacement) {
		Assert.notNull(aftersalesReplacement, "[Assertion failed] - entity is required; it must not be null");
		Assert.isTrue(aftersalesReplacement.isNew(), "[Assertion failed] - entity must be new");

		aftersalesReplacement.setId(IdWorker.getId());
		aftersalesReplacement.setCreatedDate(new Date());
		aftersalesReplacement.setVersion(0L);
		
		boolean result = aftersalesReplacementDao.save(aftersalesReplacement);
		if (CollectionUtils.isNotEmpty(aftersalesReplacement.getAftersalesItems())) {
			List<AftersalesItem> getAftersalesItems = new ArrayList<>();
			for (AftersalesItem aftersalesItem : aftersalesReplacement.getAftersalesItems()) {
				aftersalesItem.setId(IdWorker.getId());
				aftersalesItem.setCreatedDate(new Date());
				aftersalesItem.setVersion(0L);
				getAftersalesItems.add(aftersalesItem);
			}
			aftersalesItemDao.saveBatch(getAftersalesItems);
		}
		return result;
	}
	
}