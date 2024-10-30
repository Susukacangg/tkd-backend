package com.tkd.dictionaryservice.repository;

import com.tkd.dictionaryservice.entity.UsageExampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DictionaryExampleDao extends JpaRepository<UsageExampleEntity, Long> {
    List<UsageExampleEntity> findByWordId(Long wordId);

    Optional<UsageExampleEntity> findByUsageExampleId(Long usageExampleId);
}
