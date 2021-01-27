package com.igomall.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.igomall.Filter;
import com.igomall.Order;
import com.igomall.Page;
import com.igomall.Pageable;
import com.igomall.dao.MemberDao;
import com.igomall.dao.MemberDepositLogDao;
import com.igomall.dao.MemberRankDao;
import com.igomall.dao.PointLogDao;
import com.igomall.service.MemberService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.igomall.entity.BaseEntity;
import com.igomall.entity.Member;
import com.igomall.entity.MemberDepositLog;
import com.igomall.entity.MemberRank;
import com.igomall.entity.PointLog;
import com.igomall.entity.User;

/**
 * Service - 会员
 * 
 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberDao, Member> implements MemberService {

	/**
	 * E-mail身份配比
	 */
	private static final Pattern EMAIL_PRINCIPAL_PATTERN = Pattern.compile(".*@.*");

	/**
	 * 手机身份配比
	 */
	private static final Pattern MOBILE_PRINCIPAL_PATTERN = Pattern.compile("\\d+");

	/**
	 * 更新忽略属性
	 */
	private static final String[] UPDATE_IGNORE_PROPERTIES = new String[] { BaseEntity.CREATED_DATE_PROPERTY_NAME, BaseEntity.LAST_MODIFIED_DATE_PROPERTY_NAME, BaseEntity.VERSION_PROPERTY_NAME };
	
	@Inject
	private MemberDao memberDao;
	@Inject
	private MemberRankDao memberRankDao;
	@Inject
	private MemberDepositLogDao memberDepositLogDao;
	@Inject
	private PointLogDao pointLogDao;
//	@Inject
//	private MailService mailService;
//	@Inject
//	private SmsService smsService;

	@Override
	public Member find(Long id) {
		return memberDao.find(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Member> findList(Long... ids) {
		List<Member> result = new ArrayList<>();
		if (ids != null) {
			for (Long id : ids) {
				Member entity = this.find(id);
				if (entity != null) {
					result.add(entity);
				}
			}
		}
		return result;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Member getUser(Object principal) {
		Assert.notNull(principal, "[Assertion failed] - principal is required; it must not be null");
		Assert.isInstanceOf(String.class, principal);

		String value = String.valueOf(principal);
		if (EMAIL_PRINCIPAL_PATTERN.matcher(value).matches()) {
			return findByEmail(value);
		} else if (MOBILE_PRINCIPAL_PATTERN.matcher(value).matches()) {
			return findByMobile(value);
		} else {
			return findByUsername(value);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Set<String> getPermissions(User user) {
		Assert.notNull(user, "[Assertion failed] - user is required; it must not be null");
		Assert.isInstanceOf(Member.class, user);

		return Member.PERMISSIONS;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean supports(Class<?> userClass) {
		return userClass != null && Member.class.isAssignableFrom(userClass);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean usernameExists(String username) {
		return memberDao.exists("username", StringUtils.lowerCase(username));
	}

	@Override
	@Transactional(readOnly = true)
	public Member findByUsername(String username) {
		return memberDao.findByAttribute("username", StringUtils.lowerCase(username));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean emailExists(String email) {
		return memberDao.exists("email", StringUtils.lowerCase(email));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean emailUnique(Long id, String email) {
		return memberDao.unique(id, "email", StringUtils.lowerCase(email));
	}

	@Override
	@Transactional(readOnly = true)
	public Member findByEmail(String email) {
		return memberDao.findByAttribute("email", StringUtils.lowerCase(email));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean mobileExists(String mobile) {
		return memberDao.exists("mobile", StringUtils.lowerCase(mobile));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean mobileUnique(Long id, String mobile) {
		return memberDao.unique(id, "mobile", StringUtils.lowerCase(mobile));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Member> search(String keyword, Set<Member> excludes, Integer count) {
		if (StringUtils.isEmpty(keyword)) {
			return Collections.emptyList();
		}
		return memberDao.search(keyword, excludes, count);
	}

	@Override
	@Transactional(readOnly = true)
	public Member findByMobile(String mobile) {
		return memberDao.findByAttribute("mobile", StringUtils.lowerCase(mobile));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Member> findPage(Member.RankingType rankingType, Pageable pageable) {
		IPage<Member> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Member>(pageable.getPageNumber(), pageable.getPageSize());
		iPage.setRecords(memberDao.findPage(iPage, getPageable(pageable), rankingType));
		Page<Member> page = new Page<Member>(iPage.getRecords(), iPage.getTotal(), pageable);
		return page;
	}

	@Override
	public void addBalance(Member member, BigDecimal amount, MemberDepositLog.Type type, String memo) {
		Assert.notNull(member, "[Assertion failed] - member is required; it must not be null");
		Assert.notNull(amount, "[Assertion failed] - amount is required; it must not be null");
		Assert.notNull(type, "[Assertion failed] - type is required; it must not be null");

		if (amount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}

		Assert.notNull(member.getBalance(), "[Assertion failed] - member balance is required; it must not be null");
		Assert.state(member.getBalance().add(amount).compareTo(BigDecimal.ZERO) >= 0, "[Assertion failed] - member balance must be equal or greater than 0");

		member.setBalance(member.getBalance().add(amount));
		memberDao.update(member);

		MemberDepositLog memberDepositLog = new MemberDepositLog();
		memberDepositLog.setId(IdWorker.getId());
		memberDepositLog.setVersion(0L);
		memberDepositLog.setCreatedDate(new Date());
		memberDepositLog.setType(type);
		memberDepositLog.setCredit(amount.compareTo(BigDecimal.ZERO) > 0 ? amount : BigDecimal.ZERO);
		memberDepositLog.setDebit(amount.compareTo(BigDecimal.ZERO) < 0 ? amount.abs() : BigDecimal.ZERO);
		memberDepositLog.setBalance(member.getBalance());
		memberDepositLog.setMemo(memo);
		memberDepositLog.setMember(member);
		memberDepositLogDao.save(memberDepositLog);
	}

	@Override
	public void addFrozenAmount(Member member, BigDecimal amount) {
		Assert.notNull(member, "[Assertion failed] - member is required; it must not be null");
		Assert.notNull(amount, "[Assertion failed] - amount is required; it must not be null");

		if (amount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}

		Assert.notNull(member.getFrozenAmount(), "[Assertion failed] - member frozenAmount is required; it must not be null");
		Assert.state(member.getFrozenAmount().add(amount).compareTo(BigDecimal.ZERO) >= 0, "[Assertion failed] - member frozenAmount must be equal or greater than 0");

		member.setFrozenAmount(member.getFrozenAmount().add(amount));
		memberDao.update(member);
	}

	@Override
	public void addPoint(Member member, long amount, PointLog.Type type, String memo) {
		Assert.notNull(member, "[Assertion failed] - member is required; it must not be null");
		Assert.notNull(type, "[Assertion failed] - type is required; it must not be null");

		if (amount == 0) {
			return;
		}

		Assert.notNull(member.getPoint(), "[Assertion failed] - member point is required; it must not be null");
		Assert.state(member.getPoint() + amount >= 0, "[Assertion failed] - member point must be equal or greater than 0");

		member.setPoint(member.getPoint() + amount);
		memberDao.update(member);

		PointLog pointLog = new PointLog();
		pointLog.setId(IdWorker.getId());
		pointLog.setVersion(0L);
		pointLog.setCreatedDate(new Date());
		pointLog.setType(type);
		pointLog.setCredit(amount > 0 ? amount : 0L);
		pointLog.setDebit(amount < 0 ? Math.abs(amount) : 0L);
		pointLog.setBalance(member.getPoint());
		pointLog.setMemo(memo);
		pointLog.setMember(member);
		pointLogDao.save(pointLog);
	}

	@Override
	public void addAmount(Member member, BigDecimal amount) {
		Assert.notNull(member, "[Assertion failed] - member is required; it must not be null");
		Assert.notNull(amount, "[Assertion failed] - amount is required; it must not be null");

		if (amount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}

		Assert.notNull(member.getAmount(), "[Assertion failed] - member amount is required; it must not be null");
		Assert.state(member.getAmount().add(amount).compareTo(BigDecimal.ZERO) >= 0, "[Assertion failed] - member amount must be equal or greater than 0");

		member.setAmount(member.getAmount().add(amount));
		MemberRank memberRank = member.getMemberRank();
		if (memberRank != null && BooleanUtils.isFalse(memberRank.getIsSpecial())) {
			MemberRank newMemberRank = memberRankDao.findByAmount(member.getAmount());
			if (newMemberRank != null && newMemberRank.getAmount() != null && newMemberRank.getAmount().compareTo(memberRank.getAmount()) > 0) {
				member.setMemberRank(newMemberRank);
			}
		}
		memberDao.update(member);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Member> findPage(Pageable pageable) {
		IPage<Member> iPage = super.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Member>(pageable.getPageNumber(), pageable.getPageSize()), getPageable(pageable));
		Page<Member> page = new Page<Member>(iPage.getRecords(), iPage.getTotal(), pageable);
		return page;
	}
	
	@Override
	@Transactional
	public boolean save(Member member) {
		Assert.notNull(member, "[Assertion failed] - member is required; it must not be null");
		member.setId(IdWorker.getId());
		member.setVersion(0L);
		member.setCreatedDate(new Date());
		return memberDao.save(member);
	}

	@Override
	@Transactional
	public Member update(Member entity, String... ignoreProperties) {
		Assert.notNull(entity, "[Assertion failed] - entity is required; it must not be null");
		Assert.isTrue(!entity.isNew(), "[Assertion failed] - entity must not be new");

		Member persistant = this.find(entity.getId());
		if (persistant != null) {
			BeanUtils.copyProperties(entity, persistant, (String[]) ArrayUtils.addAll(ignoreProperties, UPDATE_IGNORE_PROPERTIES));
			persistant.setLastModifiedDate(new Date());
			memberDao.update(persistant);
		}
		return entity;
	}
	
	@Override
	@Transactional(readOnly = true)
	public BigDecimal totalBalance() {
		return memberDao.totalBalance();
	}

	@Override
	@Transactional(readOnly = true)
	public long count(Date beginDate, Date endDate) {
		return memberDao.count(beginDate, endDate);
	}
	
	@Override
	@Transactional(readOnly = true)
	public long total() {
		return memberDao.total();
	}

	@Override
	@Transactional
	public void delete(Long... ids) {
		memberDao.delete(Arrays.asList(ids));
	}
	
	/**
	 * 转换分页信息
	 * 
	 */
	protected QueryWrapper<Member> getPageable(Pageable pageable) {
		QueryWrapper<Member> queryWrapper = new QueryWrapper<Member>();
		// 增加搜索属性、搜索值
		String searchProperty = pageable.getSearchProperty();
		String searchValue = pageable.getSearchValue();
		if (StringUtils.isNotEmpty(searchProperty) && StringUtils.isNotEmpty(searchValue)) {
			//queryWrapper.and(wrapper -> wrapper.like(searchProperty, searchValue));
			queryWrapper.like(searchProperty, searchValue);
		}
		// 排序属性
		String orderProperty = com.igomall.util.StringUtils.camel2Underline(pageable.getOrderProperty());
		if (StringUtils.isNotEmpty(orderProperty)) {
			if (pageable.getOrderDirection().equals(com.igomall.Order.Direction.ASC)) {
				queryWrapper.orderByAsc(true, orderProperty);
			}
			if (pageable.getOrderDirection().equals(com.igomall.Order.Direction.DESC)) {
				queryWrapper.orderByDesc(true, orderProperty);
			}
		}
		// 筛选
		if (CollectionUtils.isNotEmpty(pageable.getFilters())) {
			queryWrapper = convertFilter(pageable.getFilters());
		}
		// 排序
		if (CollectionUtils.isNotEmpty(pageable.getOrders())) {
			queryWrapper = convertOrders(pageable.getOrders());
		}
		return queryWrapper;
	}
	
	/**
	 * 转换为Filter
	 * @param filters
	 * @return
	 */
	private QueryWrapper<Member> convertFilter(List<Filter> filters) {
		QueryWrapper<Member> queryWrapper = new QueryWrapper<Member>();
		if (CollectionUtils.isEmpty(filters)) {
			return queryWrapper;
		}
		for (Filter filter : filters) {
			if (filter == null) {
				continue;
			}
			String property = filter.getProperty();
			Filter.Operator operator = filter.getOperator();
			Object value = filter.getValue();
			Boolean ignoreCase = filter.getIgnoreCase();
			if (StringUtils.isEmpty(property)) {
				continue;
			}
			switch (operator) {
			case EQ:
				if (value != null) {
					if (BooleanUtils.isTrue(ignoreCase) && String.class.isAssignableFrom(property.getClass()) && value instanceof String) {
						queryWrapper.eq(property, ((String) value).toLowerCase());
					} else {
						queryWrapper.eq(property, value);
					}
				} else {
					queryWrapper.isNull(property);
				}
				break;
			case NE:
				if (value != null) {
					if (BooleanUtils.isTrue(ignoreCase) && String.class.isAssignableFrom(property.getClass()) && value instanceof String) {
						queryWrapper.ne(property, ((String) value).toLowerCase());
					} else {
						queryWrapper.ne(property, value);
					}
				} else {
					queryWrapper.isNotNull(property);
				}
				break;
			case GT:
				if (Number.class.isAssignableFrom(property.getClass()) && value instanceof Number) {
					queryWrapper.gt(property, (Number) value);
				}
				break;
			case LT:
				if (Number.class.isAssignableFrom(property.getClass()) && value instanceof Number) {
					queryWrapper.lt(property, (Number) value);
				}
				break;
			case GE:
				if (Number.class.isAssignableFrom(property.getClass()) && value instanceof Number) {
					queryWrapper.ge(property, (Number) value);
				}
				break;
			case LE:
				if (Number.class.isAssignableFrom(property.getClass()) && value instanceof Number) {
					queryWrapper.le(property, (Number) value);
				}
				break;
			case LIKE:
				if (String.class.isAssignableFrom(property.getClass()) && value instanceof String) {
					if (BooleanUtils.isTrue(ignoreCase)) {
						queryWrapper.like(property, ((String) value).toLowerCase());
					} else {
						queryWrapper.like(property, (String) value);
					}
				}
				break;
			case IN:
				queryWrapper.in(property, (String) value);
				break;
			case IS_NULL:
				queryWrapper.isNull(property);
				break;
			case IS_NOT_NULL:
				queryWrapper.isNotNull(property);
				break;
			}
		}
		return queryWrapper;
	}

	/**
	 * 转换为Order
	 * @param orders
	 * @return
	 */
	private QueryWrapper<Member> convertOrders(List<com.igomall.Order> orders) {
		QueryWrapper<Member> orderList = new QueryWrapper<Member>();
		if (CollectionUtils.isEmpty(orders)) {
			return orderList;
		}
		for (com.igomall.Order order : orders) {
			if (order == null) {
				continue;
			}
			String property = order.getProperty();
			Order.Direction direction = order.getDirection();
			if (StringUtils.isEmpty(property) || direction == null) {
				continue;
			}
			String[] columns = new String[] { property };
			switch (direction) {
			case ASC:
				orderList.orderByAsc(columns);
				break;
			case DESC:
				orderList.orderByDesc(columns);
				break;
			}
		}
		return orderList;
	}
	
}