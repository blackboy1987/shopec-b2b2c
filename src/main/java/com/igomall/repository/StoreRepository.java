package com.igomall.repository;

import com.igomall.entity.Store;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

@Component
public interface StoreRepository extends ElasticsearchRepository<Store, Long> {

}
