package com.tkd.dictionaryservice.repository;

import com.tkd.dictionaryservice.entity.ContributionReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContributionReportDao extends JpaRepository<ContributionReportEntity, Long> {
    List<ContributionReportEntity> findReportEntitiesByWordId(Long wordId);
}
