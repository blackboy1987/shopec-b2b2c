package com.igomall;

import com.igomall.entity.Product;
import com.igomall.entity.Sku;
import com.igomall.util.WebUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.util.*;

public class Demo1 {

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



    private static final String token = "UM_distinctid=1765f25746a21-0b5fa9b67e9ba4-5a301e42-384000-1765f25746b46a; PHPSESSID=n34b0474ksnjkqfmdov2l2g218; CNZZDATA1278647629=1683858113-1607910478-%7C1611449447; Hm_lvt_aef569cfc9c8fc42a8a6602fd8323716=1609421700,1609423573,1609468325,1611454283; wordpress_logged_in_955aa149fee7355a47eed3132a2e2b02=u76604001%7C1612663842%7CuqDbV9pbgY8KZ3WSj2UyCRTyCJGW0FMNQOn7LoI8OdG%7C7a266a9c6434b260deaf896389d26bdaa26f854718b18c960594a941f4e4c2a1; Hm_lpvt_aef569cfc9c8fc42a8a6602fd8323716=1611454585";

    public static String url(String url) {
        Map<String,Object> headers = new HashMap<>();
        headers.put("Cookie",token);
        String result = WebUtils.get(url,null,headers);
        return result;
    }


    public static void main(String[] args) throws Exception {
        parseDownloadInfo("https://www.hnymwl.com/go?post_id=45377");
    }

    public static List<Product> list(String url, Integer page) throws Exception {
        List<Product> products = new ArrayList<>();
        if(page>1){
            url=url+"/page/"+page;
        }
        System.out.println(url);
        String html = url(url);
        Document root = Jsoup.parse(html);
        Elements collg15 = root.getElementsByClass("col-lg-1-5");
        for (Element e:collg15) {
            Product product = new Product();
            Set<Sku> skus = new HashSet<>();
            Sku sku = new Sku();
            Element image = e.getElementsByTag("img").first();
            product.setAttributeValue0(image.parent().attr("href"));
            product.setAttributeValue1(image.attr("data-src"));
            product.setName(image.attr("alt"));
            sku.setIsDefault(true);
            product.setType(Product.Type.VIRTUAL);
            product.setCaption(product.getName());
            sku.setPrice(BigDecimal.valueOf(0.99));
            sku.setCost(BigDecimal.valueOf(0.99));
            sku.setMarketPrice(BigDecimal.valueOf(9.99));
            sku.setMaxCommission(BigDecimal.ZERO);
            sku.setStock(10000);
            skus.add(sku);
            product.setSkus(skus);
            product.setIsMarketable(true);
            product.setIsList(true);
            product.setIsTop(true);
            product.setIsDelivery(false);
            product.setMemo(product.getName());
            product.setKeyword(product.getName());
            parseIntroduction(product);
            products.add(product);
        }
        return products;
    }

    private static String parseIntroduction(Product product) throws Exception {
        String url = product.getAttributeValue0();
        Thread.sleep(300);
        String html = url(url);
        Document root = Jsoup.parse(html);
        Element element = root.getElementsByClass("entry-content").first();
        if(element==null){
            return null;
        }
        // 提取码
        Elements copypaw = root.getElementsByClass("copypaw");
        Elements downblank = root.getElementsByClass("go-downblank");
       // String info = parseDownloadInfo1(downblank.attr("href"),copypaw.text());
        product.setIntroduction(element.html());
        if(downblank!=null){
            product.setAttributeValue19(downblank.attr("href"));
        }
        if(StringUtils.isNotBlank(copypaw.text())){
            product.setAttributeValue17(copypaw.text());
        }
        // product.setMemo(info);
        return null;
    }

    public static String parseDownloadInfo1(String url,String copypaw) {
       // String result = url(url);
        String result = "<script type='text/javascript'>\n" +
                "\twindow.location='https://pan.baidu.com/s/17wMDXZaq9w4aNqpUmfIHTQ';setTimeout(function(){window.close()},2000)\n" +
                "</script>";
        Document document = Jsoup.parse(result);
        Element script = document.getElementsByTag("script").first();
        String downloadUrl = script.data().split("'")[1];
        if(StringUtils.isNotBlank(copypaw)){
            return downloadUrl+"。提取码："+copypaw;
        }
        return downloadUrl;
    }

    public static String parseDownloadInfo(String url) {
        String html = url(url);
        System.out.println("html:"+html);
        // String html = "<script type='text/javascript'>window.location='https://pan.baidu.com/s/1IiLW0MqYP5AD4cyuIHAXIw';setTimeout(function(){window.close()},2000)</script>";
        Document parse = Jsoup.parse(html);
        Element script = parse.getElementsByTag("script").first();
        String data = script.data();
        System.out.println("data:"+data);
        if(StringUtils.isNotBlank(data)){
            String[] ss = data.split(";");
            if(ss.length>1){
                String s = ss[0];
                System.out.println("s:"+s);
                ss = s.split("\\'");
                System.out.println(ss.length);
                if(ss.length>=2){
                    System.out.println("data3:"+ss[1]);
                    return ss[1];
                }
                System.out.println("data111111:"+data);
                return data;
            }

        }
        return data;
    }

    private static class ArticleCategory {

        private String name;

        private Integer page;

        private Long articleCategoryId;

        private String url;

        public ArticleCategory() {
        }

        public ArticleCategory(String name, Integer page, Long articleCategoryId,String url) {
            this.name = name;
            this.page = page;
            this.articleCategoryId = articleCategoryId;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Long getArticleCategoryId() {
            return articleCategoryId;
        }

        public void setArticleCategoryId(Long articleCategoryId) {
            this.articleCategoryId = articleCategoryId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    private static class ProductCategory {

        private String name;

        private Integer page;

        private Long productCategoryId;

        private String url;

        public ProductCategory() {
        }

        public ProductCategory(String name, Integer page, Long productCategoryId,String url) {
            this.name = name;
            this.page = page;
            this.productCategoryId = productCategoryId;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Long getProductCategoryId() {
            return productCategoryId;
        }

        public void setProductCategoryId(Long productCategoryId) {
            this.productCategoryId = productCategoryId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
