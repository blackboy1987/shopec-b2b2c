package com.igomall.dao;

import java.util.List;

import com.igomall.entity.FriendLink;
import org.apache.ibatis.annotations.Param;

/**
 * Dao - 友情链接
 * 
 */
public interface FriendLinkDao extends BaseDao<FriendLink> {

	/**
	 * 查找友情链接
	 * 
	 * @param type
	 *            类型
	 * @return 友情链接
	 */
	List<FriendLink> findList(@Param("type")FriendLink.Type type);

}