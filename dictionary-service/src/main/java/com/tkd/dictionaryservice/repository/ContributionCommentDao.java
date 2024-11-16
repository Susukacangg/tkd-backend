package com.tkd.dictionaryservice.repository;

import com.tkd.dictionaryservice.entity.ContributionCommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ContributionCommentDao extends JpaRepository<ContributionCommentEntity, Long> {
    Page<ContributionCommentEntity> findAllByWordIdOrderByCommentDateTimeDescEditedDateTimeDesc(Long wordId, Pageable pageable);

    Optional<ContributionCommentEntity> findByCommentIdEquals(Long commentId);
}
