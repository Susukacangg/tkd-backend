package com.tkd.dictionaryservice.repository;

import com.tkd.dictionaryservice.entity.TranslationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DictionaryTranslationDao extends JpaRepository<TranslationEntity, Long> {
}
