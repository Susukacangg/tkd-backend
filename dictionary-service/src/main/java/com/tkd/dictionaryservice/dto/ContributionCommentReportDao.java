package com.tkd.dictionaryservice.dto;

import com.tkd.dictionaryservice.entity.ContributionCommentReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContributionCommentReportDao extends JpaRepository<ContributionCommentReportEntity, Long> {
}
