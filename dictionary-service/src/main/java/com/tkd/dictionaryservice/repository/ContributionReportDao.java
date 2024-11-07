package com.tkd.dictionaryservice.repository;

import com.tkd.dictionaryservice.entity.ContributionReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContributionReportDao extends JpaRepository<ContributionReportEntity, Long> {
}
