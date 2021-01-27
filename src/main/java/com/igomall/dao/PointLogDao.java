package com.igomall.dao;

import java.util.List;

import com.igomall.entity.Member;
import com.igomall.entity.PointLog;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * Dao - 积分记录
 * 
 */
public interface PointLogDao extends BaseDao<PointLog> {

	/**
	 * 查找积分记录分页
	 * 
	 * @param member
	 *            会员
	 * @param pageable
	 *            分页信息
	 * @return 积分记录分页
	 */
	List<PointLog> findPage(IPage<PointLog> iPage, @Param("ew")QueryWrapper<PointLog> queryWrapper, @Param("member") Member member);

}