package com.igomall.dao;

import java.util.List;

import com.igomall.entity.Brand;
import com.igomall.entity.ProductCategory;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * Dao - 品牌
 * 
 */
public interface BrandDao extends BaseDao<Brand> {

	/**
	 * 查找品牌
	 * 
	 * @param productCategory
	 *            商品分类
	 * @return 品牌
	 */
	List<Brand> findList(@Param("ew")QueryWrapper<Brand> queryWrapper, @Param("productCategory") ProductCategory productCategory);

}