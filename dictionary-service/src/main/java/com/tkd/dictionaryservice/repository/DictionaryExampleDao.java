package com.tkd.dictionaryservice.repository;

import com.tkd.dictionaryservice.entity.DictionaryUsageExample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DictionaryExampleDao extends JpaRepository<DictionaryUsageExample, Long> {
}
