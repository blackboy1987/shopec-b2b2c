package com.igomall.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.igomall.Order;
import com.igomall.repository.ProductRepository;
import com.igomall.service.ProductService;
import com.igomall.service.SearchService;
import com.igomall.service.SnService;
import com.igomall.service.SpecificationValueService;
import com.igomall.util.SystemUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.lucene.search.SortField;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import com.igomall.Filter;
import com.igomall.Page;
import com.igomall.Pageable;
import com.igomall.Setting;
import com.igomall.dao.AttributeDao;
import com.igomall.dao.BrandDao;
import com.igomall.dao.ProductCategoryDao;
import com.igomall.dao.ProductDao;
import com.igomall.dao.ProductTagDao;
import com.igomall.dao.PromotionDao;
import com.igomall.dao.SkuDao;
import com.igomall.dao.StockLogDao;
import com.igomall.dao.StoreDao;
import com.igomall.dao.StoreProductCategoryDao;
import com.igomall.dao.StoreProductTagDao;
import com.igomall.entity.Attribute;
import com.igomall.entity.Brand;
import com.igomall.entity.Product;
import com.igomall.entity.ProductCategory;
import com.igomall.entity.ProductTag;
import com.igomall.entity.Promotion;
import com.igomall.entity.Sku;
import com.igomall.entity.Sn;
import com.igomall.entity.SpecificationItem;
import com.igomall.entity.StockLog;
import com.igomall.entity.Store;
import com.igomall.entity.StoreProductCategory;
import com.igomall.entity.StoreProductTag;

/**
 * Service - 商品
 * 
 */
@Service
public class ProductServiceImpl extends BaseServiceImpl<Product> implements ProductService {

	@Inject
	private SearchService searchService;
	@Inject
	private CacheManager cacheManager;
	@Inject
	private ProductDao productDao;
	@Inject
	private SkuDao skuDao;
	@Inject
	private SnService snService;
	@Inject
	private ProductCategoryDao productCategoryDao;
	@Inject
	private StoreProductCategoryDao storeProductCategoryDao;
	@Inject
	private BrandDao brandDao;
	@Inject
	private PromotionDao promotionDao;
	@Inject
	private ProductTagDao productTagDao;
	@Inject
	private StoreProductTagDao storeProductTagDao;
	@Inject
	private AttributeDao attributeDao;
	@Inject
	private StockLogDao stockLogDao;
	@Inject
	private StoreDao storeDao;
	@Inject
	private SpecificationValueService specificationValueService;
	@Inject
	private ProductRepository productRepository;

	@Override
	@Transactional(readOnly = true)
	public boolean snExists(String sn) {
		return productDao.exists("sn", sn);
	}

	@Override
	@Transactional(readOnly = true)
	public Product findBySn(String sn) {
		return productDao.findByAttribute("sn", sn);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Product> findList(Product.Type type, Store store, ProductCategory productCategory, StoreProductCategory storeProductCategory, Brand brand, Promotion promotion, ProductTag productTag, StoreProductTag storeProductTag, Map<Attribute, String> attributeValueMap, BigDecimal startPrice,
                                  BigDecimal endPrice, Boolean isMarketable, Boolean isList, Boolean isTop, Boolean isActive, Boolean isOutOfStock, Boolean isStockAlert, Boolean hasPromotion, Product.OrderType orderType, Integer count, List<Filter> filters, List<com.igomall.Order> orders) {
		QueryWrapper<Product> queryWrapper = createQueryWrapper(null, count, filters, orders);
		return productDao.findByWrapperList(queryWrapper, type, store, productCategory, storeProductCategory, brand, promotion, productTag, storeProductTag, attributeValueMap, startPrice, endPrice, isMarketable, isList, isTop, isActive, isOutOfStock, isStockAlert, hasPromotion, orderType);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "product", condition = "#useCache")
	public List<Product> findList(Product.Type type, Long storeId, Long productCategoryId, Long storeProductCategoryId, Long brandId, Long promotionId, Long productTagId, Long storeProductTagId, Map<Long, String> attributeValueMap, BigDecimal startPrice, BigDecimal endPrice, Boolean isMarketable,
                                  Boolean isList, Boolean isTop, Boolean isActive, Boolean isOutOfStock, Boolean isStockAlert, Boolean hasPromotion, Product.OrderType orderType, Integer count, List<Filter> filters, List<Order> orders, boolean useCache) {
		Store store = storeDao.find(storeId);
		if (storeId != null && store == null) {
			return Collections.emptyList();
		}
		ProductCategory productCategory = productCategoryDao.find(productCategoryId);
		if (productCategoryId != null && productCategory == null) {
			return Collections.emptyList();
		}
		StoreProductCategory storeProductCategory = storeProductCategoryDao.find(storeProductCategoryId);
		if (storeProductCategoryId != null && storeProductCategory == null) {
			return Collections.emptyList();
		}
		Brand brand = brandDao.find(brandId);
		if (brandId != null && brand == null) {
			return Collections.emptyList();
		}
		Promotion promotion = promotionDao.find(promotionId);
		if (promotionId != null && promotion == null) {
			return Collections.emptyList();
		}
		ProductTag productTag = productTagDao.find(productTagId);
		if (productTagId != null && productTag == null) {
			return Collections.emptyList();
		}
		StoreProductTag storeProductTag = storeProductTagDao.find(storeProductTagId);
		if (storeProductTagId != null && storeProductTag == null) {
			return Collections.emptyList();
		}
		Map<Attribute, String> map = new HashMap<>();
		if (attributeValueMap != null) {
			for (Map.Entry<Long, String> entry : attributeValueMap.entrySet()) {
				Attribute attribute = attributeDao.find(entry.getKey());
				if (attribute != null) {
					map.put(attribute, entry.getValue());
				}
			}
		}
		if (MapUtils.isNotEmpty(attributeValueMap) && MapUtils.isEmpty(map)) {
			return Collections.emptyList();
		}
		QueryWrapper<Product> queryWrapper = createQueryWrapper(null, count, filters, orders);
		return productDao.findByWrapperList(queryWrapper, type, store, productCategory, storeProductCategory, brand, promotion, productTag, storeProductTag, map, startPrice, endPrice, isMarketable, isList, isTop, isActive, isOutOfStock, isStockAlert, hasPromotion, orderType);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Product> findList(Product.RankingType rankingType, Store store, Integer count) {
		return productDao.findByRankingTypeList(rankingType, store, count);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Product> findPage(Product.Type type, Store.Type storeType, Store store, ProductCategory productCategory, StoreProductCategory storeProductCategory, Brand brand, Promotion promotion, ProductTag productTag, StoreProductTag storeProductTag, Map<Attribute, String> attributeValueMap,
                                  BigDecimal startPrice, BigDecimal endPrice, Boolean isMarketable, Boolean isList, Boolean isTop, Boolean isActive, Boolean isOutOfStock, Boolean isStockAlert, Boolean hasPromotion, Product.OrderType orderType, Pageable pageable) {
		if (startPrice != null && endPrice != null && startPrice.compareTo(endPrice) > 0) {
			BigDecimal temp = startPrice;
			startPrice = endPrice;
			endPrice = temp;
		}
		IPage<Product> iPage = getPluginsPage(pageable);
		iPage.setRecords(productDao.findPage(iPage, getPageable(pageable), type, storeType, store, productCategory, storeProductCategory, brand, promotion, productTag, storeProductTag, attributeValueMap, startPrice, endPrice, isMarketable, isList, isTop, isActive, isOutOfStock, isStockAlert, hasPromotion, orderType));
		return super.findPage(iPage, pageable);
	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public Page<Product> search(String keyword, Product.Type type, Store.Type storeType, Store store, Boolean isOutOfStock, Boolean isStockAlert, BigDecimal startPrice, BigDecimal endPrice, Product.OrderType orderType, Pageable pageable) {
		if (StringUtils.isEmpty(keyword)) {
			return Page.emptyPage(pageable);
		}

		if (pageable == null) {
			pageable = new Pageable();
		}
		
		// es原因page从0开始不是从1开始
		Integer pageNumber = 0;
		if (pageable.getPageNumber() >= 1) {
			pageNumber = pageable.getPageNumber() - 1;
		}
				
		//多条件设置
		QueryBuilder snQuery = QueryBuilders.matchPhraseQuery("sn", keyword);
		QueryBuilder nameQuery = QueryBuilders.matchQuery("name", keyword).boost(1.5F);
		QueryBuilder keywordQuery = QueryBuilders.matchQuery("keyword", keyword).boost(1.5F);
		QueryBuilder introductionQuery = QueryBuilders.matchQuery("introduction", keyword);
		MatchPhraseQueryBuilder isMarketableQuery = QueryBuilders.matchPhraseQuery("isMarketable", true);
		MatchPhraseQueryBuilder isListQuery = QueryBuilders.matchPhraseQuery("isList", true);
		MatchPhraseQueryBuilder isActiveQuery = QueryBuilders.matchPhraseQuery("isActive", true);
		
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.boolQuery().should(snQuery).should(nameQuery).should(keywordQuery).should(introductionQuery)).must(isMarketableQuery).must(isListQuery).must(isActiveQuery);

		if (type != null) {
			MatchPhraseQueryBuilder typePhraseQuery = QueryBuilders.matchPhraseQuery("type", String.valueOf(type));
			boolQueryBuilder = boolQueryBuilder.must(typePhraseQuery);
		}
		if (storeType != null) {
			MatchPhraseQueryBuilder storeTypePhraseQuery = QueryBuilders.matchPhraseQuery("store.type", String.valueOf(type));
			boolQueryBuilder = boolQueryBuilder.must(storeTypePhraseQuery);
		}
		if (store != null) {
			MatchPhraseQueryBuilder storePhraseQuery = QueryBuilders.matchPhraseQuery("store.id", String.valueOf(store.getId()));
			boolQueryBuilder = boolQueryBuilder.must(storePhraseQuery);
		}
		if (isOutOfStock != null) {
			MatchPhraseQueryBuilder isOutOfStockPhraseQuery = QueryBuilders.matchPhraseQuery("skus.isOutOfStock", String.valueOf(isOutOfStock));
			boolQueryBuilder = boolQueryBuilder.must(isOutOfStockPhraseQuery);
		}
		if (isStockAlert != null) {
			MatchPhraseQueryBuilder isStockAlertPhraseQuery = QueryBuilders.matchPhraseQuery("skus.isStockAlert", String.valueOf(isStockAlert));
			boolQueryBuilder = boolQueryBuilder.must(isStockAlertPhraseQuery);
		}
		if (startPrice != null && endPrice != null) {
			RangeQueryBuilder priceRangeQuery = QueryBuilders.rangeQuery("price").from(startPrice.doubleValue()).to(endPrice.doubleValue());
			boolQueryBuilder = boolQueryBuilder.must(priceRangeQuery);
		} else if (startPrice != null) {
			RangeQueryBuilder priceRangeQuery = QueryBuilders.rangeQuery("price").gt(startPrice.doubleValue());
			boolQueryBuilder = boolQueryBuilder.must(priceRangeQuery);
		} else if (endPrice != null) {
			RangeQueryBuilder priceRangeQuery = QueryBuilders.rangeQuery("price").lt(endPrice.doubleValue());
			boolQueryBuilder = boolQueryBuilder.must(priceRangeQuery);
		}
		SortField[] sortFields = null;
		Sort sort = null;
		if (orderType != null) {
			switch (orderType) {
			case TOP_DESC:
				sort = Sort.by(Direction.DESC, "isTop");
				break;
			case PRICE_ASC:
				sort = Sort.by(Direction.ASC, "price");
				break;
			case PRICE_DESC:
				sort = Sort.by(Direction.DESC, "price");
				break;
			case SALES_DESC:
				sort = Sort.by(Direction.DESC, "sales");
				break;
			case SCORE_DESC:
				sort = Sort.by(Direction.DESC, "score");
				break;
			case DATE_DESC:
				sort = Sort.by(Direction.DESC, "createdDate");
				break;
			}
		} else {
			//sort = Sort.by(Direction.DESC, "isTop");
			//sortFields = new SortField[] { new SortField("isTop", SortField.Type.STRING, true) };

			//sort.setSort(new org.apache.lucene.search.Sort(sortFields));

		}
		org.springframework.data.domain.Page<Product> products = productRepository.search(boolQueryBuilder, PageRequest.of(pageNumber, pageable.getPageSize(), sort));
		return new Page<>(products.getContent(), products.getSize(), pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Long count(Product.Type type, Store store, Boolean isMarketable, Boolean isList, Boolean isTop, Boolean isActive, Boolean isOutOfStock, Boolean isStockAlert) {
		return productDao.count(type, store, isMarketable, isList, isTop, isActive, isOutOfStock, isStockAlert);
	}

	@Override
	public Long count(Product.Type type, Long storeId, Boolean isMarketable, Boolean isList, Boolean isTop, Boolean isActive, Boolean isOutOfStock, Boolean isStockAlert) {
		Store store = storeDao.find(storeId);
		if (storeId != null && store == null) {
			return 0L;
		}
		return productDao.count(type, store, isMarketable, isList, isTop, isActive, isOutOfStock, isStockAlert);
	}

	@Override
	public long viewHits(Long id) {
		Assert.notNull(id, "[Assertion failed] - id is required; it must not be null");

		Ehcache cache = cacheManager.getEhcache(Product.HITS_CACHE_NAME);
		cache.acquireWriteLockOnKey(id);
		try {
			Element element = cache.get(id);
			Long hits;
			if (element != null) {
				hits = (Long) element.getObjectValue() + 1;
			} else {
				Product product = productDao.find(id);
				if (product == null) {
					return 0L;
				}
				hits = product.getHits() + 1;
			}
			cache.put(new Element(id, hits));
			return hits;
		} finally {
			cache.releaseWriteLockOnKey(id);
		}
	}

	@Override
	public void addHits(Product product, long amount) {
		Assert.notNull(product, "[Assertion failed] - product is required; it must not be null");
		Assert.state(amount >= 0, "[Assertion failed] - amount must be equal or greater than 0");

		if (amount == 0) {
			return;
		}

		Calendar nowCalendar = Calendar.getInstance();
		Calendar weekHitsCalendar = DateUtils.toCalendar(product.getWeekHitsDate());
		Calendar monthHitsCalendar = DateUtils.toCalendar(product.getMonthHitsDate());
		if (nowCalendar.get(Calendar.YEAR) > weekHitsCalendar.get(Calendar.YEAR) || nowCalendar.get(Calendar.WEEK_OF_YEAR) > weekHitsCalendar.get(Calendar.WEEK_OF_YEAR)) {
			product.setWeekHits(amount);
		} else {
			product.setWeekHits(product.getWeekHits() + amount);
		}
		if (nowCalendar.get(Calendar.YEAR) > monthHitsCalendar.get(Calendar.YEAR) || nowCalendar.get(Calendar.MONTH) > monthHitsCalendar.get(Calendar.MONTH)) {
			product.setMonthHits(amount);
		} else {
			product.setMonthHits(product.getMonthHits() + amount);
		}
		product.setHits(product.getHits() + amount);
		product.setWeekHitsDate(new Date());
		product.setMonthHitsDate(new Date());
		productDao.update(product);
	}

	@Override
	public void addSales(Product product, long amount) {
		Assert.notNull(product, "[Assertion failed] - product is required; it must not be null");
		Assert.state(amount >= 0, "[Assertion failed] - amount must be equal or greater than 0");

		if (amount == 0) {
			return;
		}

		Calendar nowCalendar = Calendar.getInstance();
		Calendar weekSalesCalendar = DateUtils.toCalendar(product.getWeekSalesDate());
		Calendar monthSalesCalendar = DateUtils.toCalendar(product.getMonthSalesDate());
		if (nowCalendar.get(Calendar.YEAR) > weekSalesCalendar.get(Calendar.YEAR) || nowCalendar.get(Calendar.WEEK_OF_YEAR) > weekSalesCalendar.get(Calendar.WEEK_OF_YEAR)) {
			product.setWeekSales(amount);
		} else {
			product.setWeekSales(product.getWeekSales() + amount);
		}
		if (nowCalendar.get(Calendar.YEAR) > monthSalesCalendar.get(Calendar.YEAR) || nowCalendar.get(Calendar.MONTH) > monthSalesCalendar.get(Calendar.MONTH)) {
			product.setMonthSales(amount);
		} else {
			product.setMonthSales(product.getMonthSales() + amount);
		}
		product.setSales(product.getSales() + amount);
		product.setWeekSalesDate(new Date());
		product.setMonthSalesDate(new Date());
		productDao.update(product);
	}

	@Override
	@CacheEvict(value = { "product", "productCategory" }, allEntries = true)
	public Product create(Product product, Sku sku) {
		Assert.notNull(product, "[Assertion failed] - product is required; it must not be null");
		Assert.isTrue(product.isNew(), "[Assertion failed] - product must be new");
		Assert.notNull(product.getType(), "[Assertion failed] - product type is required; it must not be null");
		Assert.isTrue(!product.hasSpecification(), "[Assertion failed] - product must not have specification");
		Assert.notNull(sku, "[Assertion failed] - sku is required; it must not be null");
		Assert.isTrue(sku.isNew(), "[Assertion failed] - sku must be new");
		Assert.state(!sku.hasSpecification(), "[Assertion failed] - sku must not have specification");

		switch (product.getType()) {
		case GENERAL:
			sku.setExchangePoint(0L);
			break;
		case EXCHANGE:
			sku.setPrice(BigDecimal.ZERO);
			sku.setMaxCommission(BigDecimal.ZERO);
			sku.setRewardPoint(0L);
			product.setPromotions(null);
			break;
		case GIFT:
			sku.setPrice(BigDecimal.ZERO);
			sku.setMaxCommission(BigDecimal.ZERO);
			sku.setRewardPoint(0L);
			sku.setExchangePoint(0L);
			product.setPromotions(null);
			break;
		}
		if (sku.getMarketPrice() == null) {
			sku.setMarketPrice(calculateDefaultMarketPrice(sku.getPrice()));
		}
		if (sku.getRewardPoint() == null) {
			sku.setRewardPoint(calculateDefaultRewardPoint(sku.getPrice()));
		} else {
			long maxRewardPoint = calculateMaxRewardPoint(sku.getPrice());
			sku.setRewardPoint(sku.getRewardPoint() > maxRewardPoint ? maxRewardPoint : sku.getRewardPoint());
		}
		sku.setAllocatedStock(0);
		sku.setIsDefault(true);
		sku.setProduct(product);
		sku.setSpecificationValues(null);
		sku.setCartItems(null);
		sku.setOrderItems(null);
		sku.setOrderShippingItems(null);
		sku.setProductNotifies(null);
		sku.setStockLogs(null);

		product.setPrice(sku.getPrice());
		product.setCost(sku.getCost());
		product.setMarketPrice(sku.getMarketPrice());
		product.setMaxCommission(sku.getMaxCommission().compareTo(sku.getPrice()) > 0 ? BigDecimal.ZERO : sku.getMaxCommission());
		product.setIsActive(true);
		product.setScore(0F);
		product.setTotalScore(0L);
		product.setScoreCount(0L);
		product.setHits(0L);
		product.setWeekHits(0L);
		product.setMonthHits(0L);
		product.setSales(0L);
		product.setWeekSales(0L);
		product.setMonthSales(0L);
		product.setWeekHitsDate(new Date());
		product.setMonthHitsDate(new Date());
		product.setWeekSalesDate(new Date());
		product.setMonthSalesDate(new Date());
		product.setSpecificationItems(null);
		product.setReviews(null);
		product.setConsultations(null);
		product.setProductFavorites(null);
		product.setSkus(null);
		setValue(product);
		product.setSn(StringUtils.lowerCase(product.getSn()));
		if (CollectionUtils.isNotEmpty(product.getProductImages())) {
			Collections.sort(product.getProductImages());
		}
		super.save(product);

		setValue(sku);
		skuDao.save(sku);
		stockIn(sku);

		return product;
	}

	@Override
	@CacheEvict(value = { "product", "productCategory" }, allEntries = true)
	public Product create(Product product, List<Sku> skus) {
		Assert.notNull(product, "[Assertion failed] - product is required; it must not be null");
		Assert.isTrue(product.isNew(), "[Assertion failed] - product must be new");
		Assert.notNull(product.getType(), "[Assertion failed] - product type is required; it must not be null");
		Assert.isTrue(product.hasSpecification(), "[Assertion failed] - product must have specification");
		Assert.notEmpty(skus, "[Assertion failed] - skus must not be empty: it must contain at least 1 element");

		final List<SpecificationItem> specificationItems = product.getSpecificationItems();
		if (CollectionUtils.exists(skus, new Predicate() {
			private Set<List<Integer>> set = new HashSet<>();

			public boolean evaluate(Object object) {
				Sku sku = (Sku) object;
				return sku == null || !sku.isNew() || !sku.hasSpecification() || !set.add(sku.getSpecificationValueIds()) || !specificationValueService.isValid(specificationItems, sku.getSpecificationValues());
			}
		})) {
			throw new IllegalArgumentException();
		}

		Sku defaultSku = (Sku) CollectionUtils.find(skus, new Predicate() {
			public boolean evaluate(Object object) {
				Sku sku = (Sku) object;
				return sku != null && sku.getIsDefault();
			}
		});
		if (defaultSku == null) {
			defaultSku = skus.get(0);
			defaultSku.setIsDefault(true);
		}

		for (Sku sku : skus) {
			switch (product.getType()) {
			case GENERAL:
				sku.setExchangePoint(0L);
				break;
			case EXCHANGE:
				sku.setPrice(BigDecimal.ZERO);
				sku.setMaxCommission(BigDecimal.ZERO);
				sku.setRewardPoint(0L);
				product.setPromotions(null);
				break;
			case GIFT:
				sku.setPrice(BigDecimal.ZERO);
				sku.setMaxCommission(BigDecimal.ZERO);
				sku.setRewardPoint(0L);
				sku.setExchangePoint(0L);
				product.setPromotions(null);
				break;
			}
			if (sku.getMarketPrice() == null) {
				sku.setMarketPrice(calculateDefaultMarketPrice(sku.getPrice()));
			}
			if (sku.getRewardPoint() == null) {
				sku.setRewardPoint(calculateDefaultRewardPoint(sku.getPrice()));
			} else {
				long maxRewardPoint = calculateMaxRewardPoint(sku.getPrice());
				sku.setRewardPoint(sku.getRewardPoint() > maxRewardPoint ? maxRewardPoint : sku.getRewardPoint());
			}
			if (sku != defaultSku) {
				sku.setIsDefault(false);
			}
			sku.setAllocatedStock(0);
			sku.setProduct(product);
			sku.setCartItems(null);
			sku.setOrderItems(null);
			sku.setOrderShippingItems(null);
			sku.setProductNotifies(null);
			sku.setStockLogs(null);
		}

		product.setPrice(defaultSku.getPrice());
		product.setCost(defaultSku.getCost());
		product.setMarketPrice(defaultSku.getMarketPrice());
		product.setMaxCommission(defaultSku.getMaxCommission().compareTo(defaultSku.getPrice()) > 0 ? BigDecimal.ZERO : defaultSku.getMaxCommission());
		product.setIsActive(true);
		product.setScore(0F);
		product.setTotalScore(0L);
		product.setScoreCount(0L);
		product.setHits(0L);
		product.setWeekHits(0L);
		product.setMonthHits(0L);
		product.setSales(0L);
		product.setWeekSales(0L);
		product.setMonthSales(0L);
		product.setWeekHitsDate(new Date());
		product.setMonthHitsDate(new Date());
		product.setWeekSalesDate(new Date());
		product.setMonthSalesDate(new Date());
		product.setReviews(null);
		product.setConsultations(null);
		product.setProductFavorites(null);
		product.setSkus(null);
		setValue(product);
		product.setSn(StringUtils.lowerCase(product.getSn()));
		if (CollectionUtils.isNotEmpty(product.getProductImages())) {
			Collections.sort(product.getProductImages());
		}
		super.save(product);

		for (Sku sku : skus) {
			setValue(sku);
			skuDao.save(sku);
			stockIn(sku);
		}

		return product;
	}

	@Override
	@CacheEvict(value = { "product", "productCategory" }, allEntries = true)
	public Product modify(Product product, Sku sku) {
		Assert.notNull(product, "[Assertion failed] - product is required; it must not be null");
		Assert.isTrue(!product.isNew(), "[Assertion failed] - product must not be new");
		Assert.isTrue(!product.hasSpecification(), "[Assertion failed] - product must not have specification");
		Assert.notNull(sku, "[Assertion failed] - sku is required; it must not be null");
		Assert.isTrue(sku.isNew(), "[Assertion failed] - sku must be new");
		Assert.state(!sku.hasSpecification(), "[Assertion failed] - sku must not have specification");

		Product pProduct = productDao.find(product.getId());
		switch (pProduct.getType()) {
		case GENERAL:
			sku.setExchangePoint(0L);
			break;
		case EXCHANGE:
			sku.setPrice(BigDecimal.ZERO);
			sku.setMaxCommission(BigDecimal.ZERO);
			sku.setRewardPoint(0L);
			product.setPromotions(null);
			break;
		case GIFT:
			sku.setPrice(BigDecimal.ZERO);
			sku.setMaxCommission(BigDecimal.ZERO);
			sku.setRewardPoint(0L);
			sku.setExchangePoint(0L);
			product.setPromotions(null);
			break;
		}
		if (sku.getMarketPrice() == null) {
			sku.setMarketPrice(calculateDefaultMarketPrice(sku.getPrice()));
		}
		if (sku.getRewardPoint() == null) {
			sku.setRewardPoint(calculateDefaultRewardPoint(sku.getPrice()));
		} else {
			long maxRewardPoint = calculateMaxRewardPoint(sku.getPrice());
			sku.setRewardPoint(sku.getRewardPoint() > maxRewardPoint ? maxRewardPoint : sku.getRewardPoint());
		}
		sku.setAllocatedStock(0);
		sku.setIsDefault(true);
		sku.setProduct(pProduct);
		sku.setSpecificationValues(null);
		sku.setCartItems(null);
		sku.setOrderItems(null);
		sku.setOrderShippingItems(null);
		sku.setProductNotifies(null);
		sku.setStockLogs(null);

		if (pProduct.hasSpecification()) {
			for (Sku pSku : pProduct.getSkus()) {
				skuDao.remove(pSku);
			}
			if (sku.getStock() == null) {
				throw new IllegalArgumentException();
			}
			setValue(sku);
			skuDao.save(sku);
			stockIn(sku);
		} else {
			Sku defaultSku = pProduct.getDefaultSku();
			defaultSku.setPrice(sku.getPrice());
			defaultSku.setCost(sku.getCost());
			defaultSku.setMarketPrice(sku.getMarketPrice());
			defaultSku.setMaxCommission(sku.getMaxCommission().compareTo(sku.getPrice()) > 0 ? BigDecimal.ZERO : sku.getMaxCommission());
			defaultSku.setRewardPoint(sku.getRewardPoint());
			defaultSku.setExchangePoint(sku.getExchangePoint());
			defaultSku.setLastModifiedDate(new Date());
			skuDao.update(defaultSku);
		}

		product.setPrice(sku.getPrice());
		product.setCost(sku.getCost());
		product.setMarketPrice(sku.getMarketPrice());
		product.setMaxCommission(sku.getMaxCommission().compareTo(sku.getPrice()) > 0 ? BigDecimal.ZERO : sku.getMaxCommission());
		setValue(product);
		BeanUtils.copyProperties(product, pProduct, "sn", "type", "score", "totalScore", "scoreCount", "hits", "weekHits", "monthHits", "sales", "weekSales", "monthSales", "weekHitsDate", "monthHitsDate", "weekSalesDate", "monthSalesDate", "reviews", "consultations", "productFavorites", "skus", "store");
		if (pProduct.getTotalScore() != null && pProduct.getScoreCount() != null && pProduct.getScoreCount() > 0) {
			pProduct.setScore((float) product.getTotalScore() / product.getScoreCount());
		} else {
			pProduct.setScore(0F);
		}
		if (CollectionUtils.isNotEmpty(pProduct.getProductImages())) {
			Collections.sort(pProduct.getProductImages());
		}
		super.update(pProduct);
		
		return pProduct;
	}

	@Override
	@CacheEvict(value = { "product", "productCategory" }, allEntries = true)
	public Product modify(Product product, List<Sku> skus) {
		Assert.notNull(product, "[Assertion failed] - product is required; it must not be null");
		Assert.isTrue(!product.isNew(), "[Assertion failed] - product must not be new");
		Assert.isTrue(product.hasSpecification(), "[Assertion failed] - product must have specification");
		Assert.notEmpty(skus, "[Assertion failed] - skus must not be empty: it must contain at least 1 element");

		final List<SpecificationItem> specificationItems = product.getSpecificationItems();
		if (CollectionUtils.exists(skus, new Predicate() {
			private Set<List<Integer>> set = new HashSet<>();

			public boolean evaluate(Object object) {
				Sku sku = (Sku) object;
				return sku == null || !sku.isNew() || !sku.hasSpecification() || !set.add(sku.getSpecificationValueIds()) || !specificationValueService.isValid(specificationItems, sku.getSpecificationValues());
			}
		})) {
			throw new IllegalArgumentException();
		}

		Sku defaultSku = (Sku) CollectionUtils.find(skus, new Predicate() {
			public boolean evaluate(Object object) {
				Sku sku = (Sku) object;
				return sku != null && sku.getIsDefault();
			}
		});
		if (defaultSku == null) {
			defaultSku = skus.get(0);
			defaultSku.setIsDefault(true);
		}

		Product pProduct = productDao.find(product.getId());
		for (Sku sku : skus) {
			switch (pProduct.getType()) {
			case GENERAL:
				sku.setExchangePoint(0L);
				break;
			case EXCHANGE:
				sku.setPrice(BigDecimal.ZERO);
				sku.setMaxCommission(BigDecimal.ZERO);
				sku.setRewardPoint(0L);
				product.setPromotions(null);
				break;
			case GIFT:
				sku.setPrice(BigDecimal.ZERO);
				sku.setMaxCommission(BigDecimal.ZERO);
				sku.setRewardPoint(0L);
				sku.setExchangePoint(0L);
				product.setPromotions(null);
				break;
			}
			if (sku.getMarketPrice() == null) {
				sku.setMarketPrice(calculateDefaultMarketPrice(sku.getPrice()));
			}
			if (sku.getRewardPoint() == null) {
				sku.setRewardPoint(calculateDefaultRewardPoint(sku.getPrice()));
			} else {
				long maxRewardPoint = calculateMaxRewardPoint(sku.getPrice());
				sku.setRewardPoint(sku.getRewardPoint() > maxRewardPoint ? maxRewardPoint : sku.getRewardPoint());
			}
			if (sku != defaultSku) {
				sku.setIsDefault(false);
			}
			sku.setMaxCommission(sku.getMaxCommission().compareTo(sku.getPrice()) > 0 ? BigDecimal.ZERO : sku.getMaxCommission());
			sku.setAllocatedStock(0);
			sku.setProduct(pProduct);
			sku.setCartItems(null);
			sku.setOrderItems(null);
			sku.setOrderShippingItems(null);
			sku.setProductNotifies(null);
			sku.setStockLogs(null);
		}

		if (pProduct.hasSpecification()) {
			for (Sku pSku : pProduct.getSkus()) {
				if (!exists(skus, pSku.getSpecificationValueIds())) {
					skuDao.remove(pSku);
				}
			}
			for (Sku sku : skus) {
				Sku pSku = find(pProduct.getSkus(), sku.getSpecificationValueIds());
				if (pSku != null) {
					pSku.setPrice(sku.getPrice());
					pSku.setCost(sku.getCost());
					pSku.setMarketPrice(sku.getMarketPrice());
					pSku.setMaxCommission(sku.getMaxCommission().compareTo(sku.getPrice()) > 0 ? BigDecimal.ZERO : sku.getMaxCommission());
					pSku.setRewardPoint(sku.getRewardPoint());
					pSku.setExchangePoint(sku.getExchangePoint());
					pSku.setIsDefault(sku.getIsDefault());
					pSku.setSpecificationValues(sku.getSpecificationValues());
					pSku.setLastModifiedDate(new Date());
					skuDao.update(pSku);
				} else {
					if (sku.getStock() == null) {
						throw new IllegalArgumentException();
					}
					setValue(sku);
					skuDao.save(sku);
					stockIn(sku);
				}
			}
		} else {
			skuDao.remove(pProduct.getDefaultSku());
			for (Sku sku : skus) {
				if (sku.getStock() == null) {
					throw new IllegalArgumentException();
				}
				setValue(sku);
				skuDao.save(sku);
				stockIn(sku);
			}
		}

		product.setPrice(defaultSku.getPrice());
		product.setCost(defaultSku.getCost());
		product.setMarketPrice(defaultSku.getMarketPrice());
		product.setMaxCommission(defaultSku.getMaxCommission().compareTo(defaultSku.getPrice()) > 0 ? BigDecimal.ZERO : defaultSku.getMaxCommission());
		setValue(product);
		BeanUtils.copyProperties(product, pProduct, "sn", "type", "score", "totalScore", "scoreCount", "hits", "weekHits", "monthHits", "sales", "weekSales", "monthSales", "weekHitsDate", "monthHitsDate", "weekSalesDate", "monthSalesDate", "reviews", "consultations", "productFavorites", "skus", "store");
		if (pProduct.getTotalScore() != null && pProduct.getScoreCount() != null && pProduct.getScoreCount() > 0) {
			pProduct.setScore((float) pProduct.getTotalScore() / pProduct.getScoreCount());
		} else {
			pProduct.setScore(0F);
		}
		if (CollectionUtils.isNotEmpty(pProduct.getProductImages())) {
			Collections.sort(pProduct.getProductImages());
		}
		super.update(pProduct);
		
		return pProduct;
	}

	@Override
	@CacheEvict(value = { "product", "productCategory" }, allEntries = true)
	public void refreshExpiredStoreProductActive() {
		productDao.refreshExpiredStoreProductActive();
		searchService.index(Product.class);
	}

	@Override
	@CacheEvict(value = { "product", "productCategory" }, allEntries = true)
	public void refreshActive(Store store) {
		Assert.notNull(store, "[Assertion failed] - store is required; it must not be null");

		productDao.refreshActive(store);
		searchService.index(Product.class);
	}

	@Override
	@CacheEvict(value = { "product", "productCategory" }, allEntries = true)
	public void shelves(Long[] ids) {
		Assert.notEmpty(ids, "[Assertion failed] - ids must not be empty: it must contain at least 1 element");

		productDao.shelves(ids);
		// 演示环境不启用，正式可以启用
		// searchService.index(Product.class);
	}

	@Override
	@CacheEvict(value = { "product", "productCategory" }, allEntries = true)
	public void shelf(Long[] ids) {
		Assert.notEmpty(ids, "[Assertion failed] - ids must not be empty: it must contain at least 1 element");

		productDao.shelf(ids);
		// 演示环境不启用，正式可以启用
		// searchService.index(Product.class);
	}

    @Override
    public boolean nameExist(String name) {
		return productDao.exists("name",name);
    }

    @Override
	@Transactional
	@CacheEvict(value = { "product", "productCategory" }, allEntries = true)
	public boolean save(Product product) {
		return super.save(product);
	}

	@Override
	@Transactional
	@CacheEvict(value = { "product", "productCategory" }, allEntries = true)
	public Product update(Product product) {
		return super.update(product);
	}

	@Override
	@Transactional
	@CacheEvict(value = { "product", "productCategory" }, allEntries = true)
	public Product update(Product product, String... ignoreProperties) {
		return super.update(product, ignoreProperties);
	}

	@Override
	@Transactional
	@CacheEvict(value = { "product", "productCategory" }, allEntries = true)
	public void delete(Long id) {
		productDao.delete(Arrays.asList(id));
	}

	@Override
	@Transactional
	@CacheEvict(value = { "product", "productCategory" }, allEntries = true)
	public void delete(Long... ids) {
		productDao.delete(Arrays.asList(ids));
	}

	@Override
	@Transactional
	@CacheEvict(value = { "product", "productCategory" }, allEntries = true)
	public void delete(Product product) {
		productDao.delete(Arrays.asList(product.getId()));
	}

	/**
	 * 设置商品值
	 * 
	 * @param product
	 *            商品
	 */
	private void setValue(Product product) {
		if (product == null) {
			return;
		}

		if (product.isNew()) {
			if (StringUtils.isEmpty(product.getSn())) {
				String sn;
				do {
					sn = snService.generate(Sn.Type.PRODUCT);
				} while (snExists(sn));
				product.setSn(sn);
			}
		}
	}

	/**
	 * 设置SKU值
	 * 
	 * @param sku
	 *            SKU
	 */
	private void setValue(Sku sku) {
		if (sku == null) {
			return;
		}

		if (sku.isNew()) {
			Product product = sku.getProduct();
			if (product != null && StringUtils.isNotEmpty(product.getSn())) {
				String sn;
				int i = sku.hasSpecification() ? 1 : 0;
				do {
					sn = product.getSn() + (i == 0 ? StringUtils.EMPTY : "_" + i);
					i++;
				} while (skuDao.exists("sn", sn));
				sku.setSn(sn);
				sku.setId(IdWorker.getId());
				sku.setCreatedDate(new Date());
				sku.setVersion(0L);
			}
		}
	}

	/**
	 * 计算默认市场价
	 * 
	 * @param price
	 *            价格
	 * @return 默认市场价
	 */
	private BigDecimal calculateDefaultMarketPrice(BigDecimal price) {
		Assert.notNull(price, "[Assertion failed] - price is required; it must not be null");

		Setting setting = SystemUtils.getSetting();
		Double defaultMarketPriceScale = setting.getDefaultMarketPriceScale();
		return defaultMarketPriceScale != null ? setting.setScale(price.multiply(new BigDecimal(String.valueOf(defaultMarketPriceScale)))) : BigDecimal.ZERO;
	}

	/**
	 * 计算默认赠送积分
	 * 
	 * @param price
	 *            价格
	 * @return 默认赠送积分
	 */
	private long calculateDefaultRewardPoint(BigDecimal price) {
		Assert.notNull(price, "[Assertion failed] - price is required; it must not be null");

		Setting setting = SystemUtils.getSetting();
		Double defaultPointScale = setting.getDefaultPointScale();
		return defaultPointScale != null ? price.multiply(new BigDecimal(String.valueOf(defaultPointScale))).longValue() : 0L;
	}

	/**
	 * 计算最大赠送积分
	 * 
	 * @param price
	 *            价格
	 * @return 最大赠送积分
	 */
	private long calculateMaxRewardPoint(BigDecimal price) {
		Assert.notNull(price, "[Assertion failed] - price is required; it must not be null");

		Setting setting = SystemUtils.getSetting();
		Double maxPointScale = setting.getMaxPointScale();
		return maxPointScale != null ? price.multiply(new BigDecimal(String.valueOf(maxPointScale))).longValue() : 0L;
	}

	/**
	 * 根据规格值ID查找SKU
	 * 
	 * @param skus
	 *            SKU
	 * @param specificationValueIds
	 *            规格值ID
	 * @return SKU
	 */
	private Sku find(Collection<Sku> skus, final List<Integer> specificationValueIds) {
		if (CollectionUtils.isEmpty(skus) || CollectionUtils.isEmpty(specificationValueIds)) {
			return null;
		}

		return (Sku) CollectionUtils.find(skus, new Predicate() {
			public boolean evaluate(Object object) {
				Sku sku = (Sku) object;
				return sku != null && sku.getSpecificationValueIds() != null && sku.getSpecificationValueIds().equals(specificationValueIds);
			}
		});
	}

	/**
	 * 根据规格值ID判断SKU是否存在
	 * 
	 * @param skus
	 *            SKU
	 * @param specificationValueIds
	 *            规格值ID
	 * @return SKU是否存在
	 */
	private boolean exists(Collection<Sku> skus, final List<Integer> specificationValueIds) {
		return find(skus, specificationValueIds) != null;
	}

	/**
	 * 入库
	 * 
	 * @param sku
	 *            SKU
	 */
	private void stockIn(Sku sku) {
		if (sku == null || sku.getStock() == null || sku.getStock() <= 0) {
			return;
		}

		StockLog stockLog = new StockLog();
		stockLog.setType(StockLog.Type.STOCK_IN);
		stockLog.setInQuantity(sku.getStock());
		stockLog.setOutQuantity(0);
		stockLog.setStock(sku.getStock());
		stockLog.setMemo(null);
		stockLog.setSku(sku);
		stockLog.setId(IdWorker.getId());
		stockLog.setCreatedDate(new Date());
		stockLog.setVersion(0L);
		stockLogDao.save(stockLog);
	}

}