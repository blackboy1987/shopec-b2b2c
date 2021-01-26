package net.shopec.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import net.shopec.Setting;
import net.shopec.dao.ProductDao;
import net.shopec.entity.Article;
import net.shopec.entity.Product;
import net.shopec.entity.SitemapUrl;
import net.shopec.service.ArticleService;
import net.shopec.service.SitemapUrlService;
import net.shopec.util.SystemUtils;

/**
 * Service - Sitemap URL
 * 
 */
@Service
public class SitemapUrlServiceImpl implements SitemapUrlService {

	@Inject
	private ArticleService articleService;
	@Inject
	private ProductDao productDao;

	@Override
	@Transactional(readOnly = true)
	public List<SitemapUrl> generate(SitemapUrl.Type type, SitemapUrl.Changefreq changefreq, float priority, Integer first, Integer count) {
		Assert.notNull(type, "[Assertion failed] - type is required; it must not be null");
		Assert.notNull(changefreq, "[Assertion failed] - changefreq is required; it must not be null");

		Setting setting = SystemUtils.getSetting();
		List<SitemapUrl> sitemapUrls = new ArrayList<>();
		switch (type) {
		case ARTICLE:
			List<Article> articles = articleService.findList(first, count, null, null);
			for (Article article : articles) {
				SitemapUrl sitemapUrl = new SitemapUrl();
				sitemapUrl.setLoc(setting.getSiteUrl() + article.getPath());
				sitemapUrl.setLastmod(article.getLastModifiedDate());
				sitemapUrl.setChangefreq(changefreq);
				sitemapUrl.setPriority(priority);
				sitemapUrls.add(sitemapUrl);
			}
			break;
		case PRODUCT:
			List<Product> products = productDao.findList(null, null, true, true, null, null, first, count);
			for (Product product : products) {
				SitemapUrl sitemapUrl = new SitemapUrl();
				sitemapUrl.setLoc(setting.getSiteUrl() + product.getPath());
				sitemapUrl.setLastmod(product.getLastModifiedDate());
				sitemapUrl.setChangefreq(changefreq);
				sitemapUrl.setPriority(priority);
				sitemapUrls.add(sitemapUrl);
			}
			break;
		}
		return sitemapUrls;
	}

	@Override
	@Transactional(readOnly = true)
	public Long count(SitemapUrl.Type type) {
		Assert.notNull(type, "[Assertion failed] - type is required; it must not be null");

		switch (type) {
		case ARTICLE:
			return (long) articleService.count();
		case PRODUCT:
			return productDao.count(null, null, true, null, null, true, null, null);
		}
		return 0L;
	}

}