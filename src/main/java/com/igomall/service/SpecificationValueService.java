package com.igomall.service;

import java.util.List;

import com.igomall.entity.SpecificationItem;
import com.igomall.entity.SpecificationValue;

/**
 * Service - 规格值
 * 
 */
public interface SpecificationValueService {

	/**
	 * 规格值验证
	 * 
	 * @param specificationItems
	 *            规格项
	 * @param specificationValues
	 *            规格值
	 * @return 验证结果
	 */
	boolean isValid(List<SpecificationItem> specificationItems, List<SpecificationValue> specificationValues);

}