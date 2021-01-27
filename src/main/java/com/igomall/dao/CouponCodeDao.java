package com.igomall.dao;

import java.util.List;

import com.igomall.entity.Coupon;
import com.igomall.entity.CouponCode;
import com.igomall.entity.Member;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * Dao - 优惠码
 * 
 */
public interface CouponCodeDao extends BaseDao<CouponCode> {

	/**
	 * 查找优惠码分页
	 * 
	 * @param member
	 *            会员
	 * @param pageable
	 *            分页信息
	 * @return 优惠码分页
	 */
	List<CouponCode> findPage(IPage<CouponCode> iPage, @Param("ew")QueryWrapper<CouponCode> queryWrapper, Member member);

	/**
	 * 查找优惠码数量
	 * 
	 * @param coupon
	 *            优惠券
	 * @param member
	 *            会员
	 * @param hasBegun
	 *            是否已开始
	 * @param hasExpired
	 *            是否已过期
	 * @param isUsed
	 *            是否已使用
	 * @return 优惠码数量
	 */
	Long count(@Param("coupon") Coupon coupon, @Param("member")Member member, @Param("hasBegun")Boolean hasBegun, @Param("hasExpired")Boolean hasExpired, @Param("isUsed")Boolean isUsed);

}