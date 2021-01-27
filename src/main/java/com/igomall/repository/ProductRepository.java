package com.igomall.repository;

import com.igomall.entity.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

@Component
public interface ProductRepository extends ElasticsearchRepository<Product, Long> {

}
