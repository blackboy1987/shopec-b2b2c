package com.igomall.controller;

import com.igomall.Demo1;
import com.igomall.Results;
import com.igomall.controller.admin.BaseController;
import com.igomall.entity.*;
import com.igomall.service.*;
import com.igomall.entity.*;
import com.igomall.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@RestController("HnymwlController")
@RequestMapping("/hnymwl")
public class HnymwlController extends BaseController {

    @Inject
    private ProductService productService;
    @Autowired
    private ProductCategoryService productCategoryService;
    @Inject
    private SkuService skuService;
    @Autowired
    private SnService snService;
    @Autowired
    private StoreService storeService;
    @Inject
    private ArticleService articleService;
    @Inject
    private ArticleCategoryService articleCategoryService;
    @Inject
    private ProductTagService productTagService;

    public static final Map<Long,String> maps = new HashMap<>();


    static {
        maps.put(10252L,"https://www.hnymwl.com/cms/wordpress/meihua");
        maps.put(10253L,"https://www.hnymwl.com/cms/wordpress/plugins");
        maps.put(10254L,"https://www.hnymwl.com/cms/wordpress/themes");
        maps.put(10257L,"https://www.hnymwl.com/cms/dedecms");
        maps.put(10258L,"https://www.hnymwl.com/cms/think");
        maps.put(10259L,"https://www.hnymwl.com/cms/discuz");
        maps.put(10260L,"https://www.hnymwl.com/cms/ecshop");
        maps.put(10261L,"https://www.hnymwl.com/cms/diguo");
        maps.put(10262L,"https://www.hnymwl.com/cms/apple");
        maps.put(10263L,"https://www.hnymwl.com/wangzhan/h5");
        maps.put(10264L,"https://www.hnymwl.com/wangzhan/html");
        maps.put(10265L,"https://www.hnymwl.com/wangzhan/qiyeyuanma");
        maps.put(10266L,"https://www.hnymwl.com/wangzhan/qukuailian");
        maps.put(10267L,"https://www.hnymwl.com/wangzhan/shangcheng");
        maps.put(10268L,"https://www.hnymwl.com/wangzhan/tupianyuanma");
        maps.put(10269L,"https://www.hnymwl.com/wangzhan/dianyingyuanma");
        maps.put(10270L,"https://www.hnymwl.com/wangzhan/xiaoshuoyuanma");
        maps.put(10271L,"https://www.hnymwl.com/wangzhan/zhifuyuanma");
        maps.put(10272L,"https://www.hnymwl.com/wangzhan/zfxx");
        maps.put(10273L,"https://www.hnymwl.com/wangzhan/taokeyuanma");
        maps.put(10274L,"https://www.hnymwl.com/wangzhan/zhiboyuanma");
        maps.put(10275L,"https://www.hnymwl.com/wangzhan/hyzz");
        maps.put(10276L,"https://www.hnymwl.com/wangzhan/jrlc");
        maps.put(10278L,"https://www.hnymwl.com/game/shouyou");
        maps.put(10279L,"https://www.hnymwl.com/game/duanyou");
        maps.put(10280L,"https://www.hnymwl.com/game/yeyou");
    }

    @GetMapping("/init")
    public ResponseEntity init() throws Exception {
        List<ProductTag> productTags = productTagService.findAll();
        Store currentStore = storeService.find(10001L);
        for (Long key:maps.keySet()) {
            System.out.println(maps.get(key));
            ProductCategory productCategory = productCategoryService.find(key);
            boolean flag = true;
            for (int i=1;i<100;i++) {
                if(flag){
                    List<Product> products = Demo1.list(maps.get(key), i);
                    System.out.println("page:"+i+";count:"+products.size());
                    if(products.size()<10){
                        flag = false;
                    }
                    for (Product product:products) {
                        if(productService.nameExist(product.getName())){
                            continue;
                        }
                        if(StringUtils.isBlank(product.getSn())){
                            product.setSn(snService.generate(Sn.Type.PRODUCT));
                        }
                        if (productCategory == null) {
                            return Results.UNPROCESSABLE_ENTITY;
                        }
                        // 设置图片
                        List<ProductImage> productImages = new ArrayList<>();
                        ProductImage productImage = new ProductImage();
                        productImage.setLarge(product.getAttributeValue1());;
                        productImage.setMedium(product.getAttributeValue1());
                        productImage.setSource(product.getAttributeValue1());
                        productImage.setThumbnail(product.getAttributeValue1());
                        productImage.setOrder(1);
                        productImages.add(productImage);
                        product.setProductImages(productImages);
                        product.setStore(currentStore);
                        product.setProductCategory(productCategory);
                        product.setProductTags(new HashSet<>(productTags));
                        // product.removeAttributeValue();

                        if (!isValid(product, BaseEntity.Save.class)) {
                            return Results.UNPROCESSABLE_ENTITY;
                        }
                        if (StringUtils.isNotEmpty(product.getSn()) && productService.snExists(product.getSn())) {
                            return Results.UNPROCESSABLE_ENTITY;
                        }
                        System.out.println(product.getSkus());
                        List<Sku> collect = product.getSkus().stream().collect(Collectors.toList());
                        Sku sku = collect.get(0);
                        if (sku == null || !isValid(sku, getValidationGroup(product.getType()), BaseEntity.Save.class)) {
                            return Results.UNPROCESSABLE_ENTITY;
                        }
                        try{
                            productService.create(product, sku);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return ResponseEntity.ok("ok");
    }


    @GetMapping("/page")
    public ResponseEntity<?> parse(String url, Integer page, Long productCategoryId,Integer start) throws Exception {
        List<ProductTag> productTags = productTagService.findAll();
        if(start==null){
            start = 1;
        }
        Store currentStore = storeService.find(10001L);
        ProductCategory productCategory = productCategoryService.find(productCategoryId);
        for (int i=start;i<=page;i++) {
            List<Product> products = Demo1.list(url,i);
            for (Product product:products) {
                if(productService.nameExist(product.getName())){
                    continue;
                }
                if(StringUtils.isBlank(product.getSn())){
                    product.setSn(snService.generate(Sn.Type.PRODUCT));
                }
                if (productCategory == null) {
                    return Results.UNPROCESSABLE_ENTITY;
                }
                // 设置图片
                List<ProductImage> productImages = new ArrayList<>();
                ProductImage productImage = new ProductImage();
                productImage.setLarge(product.getAttributeValue1());;
                productImage.setMedium(product.getAttributeValue1());
                productImage.setSource(product.getAttributeValue1());
                productImage.setThumbnail(product.getAttributeValue1());
                productImage.setOrder(1);
                productImages.add(productImage);
                product.setProductImages(productImages);
                product.setStore(currentStore);
                product.setProductCategory(productCategory);
                product.setProductTags(new HashSet<>(productTags));
                // product.removeAttributeValue();

                if (!isValid(product, BaseEntity.Save.class)) {
                    return Results.UNPROCESSABLE_ENTITY;
                }
                if (StringUtils.isNotEmpty(product.getSn()) && productService.snExists(product.getSn())) {
                    return Results.UNPROCESSABLE_ENTITY;
                }
                System.out.println(product.getSkus());
                List<Sku> collect = product.getSkus().stream().collect(Collectors.toList());
                Sku sku = collect.get(0);
                if (sku == null || !isValid(sku, getValidationGroup(product.getType()), BaseEntity.Save.class)) {
                    return Results.UNPROCESSABLE_ENTITY;
                }
                try{
                    productService.create(product, sku);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            System.out.println(url+"===========================================================:"+i);
        }
        return Results.OK;
    }

    /**
     * 根据类型获取验证组
     *
     * @param type
     *            类型
     * @return 验证组
     */
    private Class<?> getValidationGroup(Product.Type type) {
        Assert.notNull(type, "[Assertion failed] - type is required; it must not be null");

        switch (type) {
            case GENERAL:
                return Sku.General.class;
            case EXCHANGE:
                return Sku.Exchange.class;
            case GIFT:
                return Sku.Gift.class;
            case VIRTUAL:
                return Sku.General.class;
            default:
                break;
        }
        return null;
    }
}
