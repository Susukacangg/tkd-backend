package com.tkd.dictionaryservice.repository;

import com.tkd.dictionaryservice.entity.WordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DictionaryWordDao extends JpaRepository<WordEntity, Long> {
}
