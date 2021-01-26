package net.shopec.dao;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import net.shopec.entity.Coupon;
import net.shopec.entity.Store;

/**
 * Dao - 优惠券
 * 
 */
public interface CouponDao extends BaseDao<Coupon> {

	/**
	 * 查找优惠券
	 * 
	 * @param store
	 *            店铺
	 * @param isEnabled
	 *            是否启用
	 * @param isExchange
	 *            是否允许积分兑换
	 * @param hasExpired
	 *            是否已过期
	 * @return 优惠券
	 */
	List<Coupon> findList(@Param("store")Store store, @Param("isEnabled")Boolean isEnabled, @Param("isExchange")Boolean isExchange, @Param("hasExpired")Boolean hasExpired);

	/**
	 * 查找优惠券
	 * 
	 * @param store
	 *            店铺
	 * @param matchs
	 *            匹配优惠券
	 * @return 优惠券
	 */
	List<Coupon> selectList(@Param("store")Store store, @Param("matchs")Set<Coupon> matchs);

	/**
	 * 查找优惠券分页
	 * 
	 * @param isEnabled
	 *            是否启用
	 * @param isExchange
	 *            是否允许积分兑换
	 * @param hasExpired
	 *            是否已过期
	 * @param pageable
	 *            分页信息
	 * @return 优惠券分页
	 */
	List<Coupon> findPage(IPage<Coupon> iPage, @Param("ew")QueryWrapper<Coupon> queryWrapper, @Param("isEnabled")Boolean isEnabled, @Param("isExchange")Boolean isExchange, @Param("hasExpired")Boolean hasExpired);

	/**
	 * 查找优惠券分页
	 * 
	 * @param store
	 *            店铺
	 * @param pageable
	 *            分页信息
	 * @return 优惠券分页
	 */
	List<Coupon> findPageStore(IPage<Coupon> iPage, @Param("ew")QueryWrapper<Coupon> queryWrapper, @Param("store")Store store);

}