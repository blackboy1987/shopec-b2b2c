package net.shopec.dao;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import net.shopec.entity.MemberRank;
import net.shopec.entity.ProductCategory;
import net.shopec.entity.Promotion;
import net.shopec.entity.Store;
import net.shopec.plugin.PromotionPlugin;

/**
 * Dao - 促销
 * 
 */
public interface PromotionDao extends BaseDao<Promotion> {

	/**
	 * 通过名称查找促销
	 * 
	 * @param keyword
	 *            关键词
	 * @param excludes
	 *            排除促销
	 * @param count
	 *            数量
	 * @return 促销
	 */
	List<Promotion> search(@Param("keyword")String keyword, @Param("excludes")Set<Promotion> excludes, @Param("count")Integer count);

	/**
	 * 查找促销
	 * 
	 * @param promotionPlugin
	 *            促销插件
	 * @param store
	 *            店铺
	 * @param isEnabled
	 *            是否开启
	 * @return 促销
	 */
	List<Promotion> findList(@Param("promotionPlugin")PromotionPlugin promotionPlugin, @Param("store")Store store, @Param("isEnabled")Boolean isEnabled);

	/**
	 * 查找促销
	 * 
	 * @param promotionPlugin
	 *            促销插件
	 * @param store
	 *            店铺
	 * @param memberRank
	 *            会员等级
	 * @param productCategory
	 *            商品分类
	 * @param hasBegun
	 *            是否已开始
	 * @param hasEnded
	 *            是否已结束
	 * @return 促销
	 */
	List<Promotion> selectList(@Param("ew")QueryWrapper<Promotion> wrapper, @Param("promotionPlugin")PromotionPlugin promotionPlugin, @Param("store")Store store, @Param("memberRank")MemberRank memberRank, @Param("productCategory")ProductCategory productCategory, @Param("hasBegun")Boolean hasBegun, @Param("hasEnded")Boolean hasEnded);

	/**
	 * 查找促销
	 * 
	 * @param promotionPlugin
	 *            促销插件
	 * @param store
	 *            店铺
	 * @return 促销分页
	 */
	List<Promotion> findPage(IPage<Promotion> iPage, @Param("ew")QueryWrapper<Promotion> queryWrapper, PromotionPlugin promotionPlugin, Store store);

}